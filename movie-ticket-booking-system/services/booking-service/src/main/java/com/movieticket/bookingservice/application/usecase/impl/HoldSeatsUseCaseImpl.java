package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.HoldSeatsResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.application.command.HoldSeatsCommand;
import com.movieticket.bookingservice.application.usecase.HoldSeatsUseCase;
import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.entity.SeatHold;
import com.movieticket.bookingservice.domain.entity.SeatHoldSeat;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import com.movieticket.bookingservice.domain.enums.TicketStatus;
import com.movieticket.bookingservice.domain.port.BookingEventOutboxRepository;
import com.movieticket.bookingservice.domain.port.SeatHoldRepository;
import com.movieticket.bookingservice.domain.port.TicketRepository;
import com.movieticket.bookingservice.infrastructure.client.ShowtimeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HoldSeatsUseCaseImpl implements HoldSeatsUseCase {

    private final SeatHoldRepository seatHoldRepository;
    private final TicketRepository ticketRepository;
    private final BookingEventOutboxRepository outboxRepository;
    private final ShowtimeClient showtimeClient;

    private static final int HOLD_DURATION_MINUTES = 10;

    @Override
    @Transactional
    public HoldSeatsResponse execute(HoldSeatsCommand command) {
        Long showtimeId = command.getShowtimeId();
        List<String> seatCodes = command.getSeatCodes();
        LocalDateTime now = LocalDateTime.now();

        for (String seatCode : seatCodes) {
            if (seatHoldRepository.existsActiveHoldForSeat(showtimeId, seatCode, now)) {
                throw new ApiException(ErrorCode.SEAT_ALREADY_HELD,
                        "Seat " + seatCode + " is already held by another user");
            }
            if (ticketRepository.existsByShowtimeIdAndSeatCodeAndStatusIn(showtimeId, seatCode,
                    List.of(TicketStatus.ACTIVE, TicketStatus.USED))) {
                throw new ApiException(ErrorCode.SEAT_UNAVAILABLE,
                        "Seat " + seatCode + " is already booked");
            }
        }

        String holdToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = now.plusMinutes(HOLD_DURATION_MINUTES);

        List<SeatHoldSeat> holdSeats = seatCodes.stream()
                .map(code -> SeatHoldSeat.builder()
                        .showtimeId(showtimeId)
                        .seatCode(code)
                        .createdAt(now)
                        .build())
                .collect(Collectors.toList());

        SeatHold seatHold = SeatHold.builder()
                .holdToken(holdToken)
                .userId(command.getUserId())
                .showtimeId(showtimeId)
                .status(SeatHoldStatus.ACTIVE)
                .expiresAt(expiresAt)
                .createdAt(now)
                .updatedAt(now)
                .seats(holdSeats)
                .build();

        seatHoldRepository.save(seatHold);

        BookingEventOutbox outbox = BookingEventOutbox.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType("SeatHold")
                .aggregateId(holdToken)
                .eventType("SEAT_HOLD_CREATED")
                .topic("booking.seat-hold.created")
                .payloadJson("{\"holdToken\":\"" + holdToken + "\",\"userId\":" + command.getUserId()
                        + ",\"showtimeId\":" + showtimeId + ",\"expiresAt\":\"" + expiresAt + "\"}")
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();
        outboxRepository.save(outbox);

        List<HoldSeatsResponse.SeatHoldSeatDto> seatDtos = seatCodes.stream()
                .map(code -> HoldSeatsResponse.SeatHoldSeatDto.builder().seatCode(code).build())
                .collect(Collectors.toList());

        return HoldSeatsResponse.builder()
                .holdToken(holdToken)
                .expiresAt(expiresAt)
                .seats(seatDtos)
                .build();
    }
}
