package com.movieticket.bookingservice.api.controller;

import com.movieticket.bookingservice.api.dto.*;
import com.movieticket.bookingservice.application.command.CancelBookingCommand;
import com.movieticket.bookingservice.application.command.ConfirmBookingCommand;
import com.movieticket.bookingservice.application.command.HoldSeatsCommand;
import com.movieticket.bookingservice.application.usecase.CancelBookingUseCase;
import com.movieticket.bookingservice.application.usecase.ConfirmBookingUseCase;
import com.movieticket.bookingservice.application.usecase.HoldSeatsUseCase;
import com.movieticket.bookingservice.domain.entity.Booking;
import com.movieticket.bookingservice.domain.entity.Ticket;
import com.movieticket.bookingservice.domain.port.BookingRepository;
import com.movieticket.bookingservice.domain.port.TicketRepository;
import com.movieticket.bookingservice.infrastructure.client.MovieClient;
import com.movieticket.bookingservice.infrastructure.security.BookingContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {

        private final HoldSeatsUseCase holdSeatsUseCase;
        private final ConfirmBookingUseCase confirmBookingUseCase;
        private final CancelBookingUseCase cancelBookingUseCase;
        private final BookingRepository bookingRepository;
        private final TicketRepository ticketRepository;
        private final BookingContext bookingContext;

        private final MovieClient movieClient;

        @GetMapping("/demo")
        public String testDemo(HttpServletRequest request) {
                String correlationId = request.getHeader("X-Correlation-ID");
                String userId = request.getHeader("X-User-ID");
                String userEmail = request.getHeader("X-User-Email");

                log.info("============== BOOKING SERVICE ==============");
                log.info("=> [1. Booking Service] Nhận request từ Gateway");
                log.info("=> Correlation ID: {}", correlationId);
                log.info("=> User ID: {}", userId);
                log.info("=> User Email: {}", userEmail);
                log.info("=> Đang gọi Movie Service bằng Feign Client...");
                log.info("=============================================");

                String result = movieClient.callMovieDemo();

                return "Hệ thống kết nối mượt mà! " + result;
        }

        @PostMapping("/hold-seats")
        public ResponseEntity<ApiResponse<HoldSeatsResponse>> holdSeats(@Valid @RequestBody HoldSeatsRequest request) {
                Long userId = bookingContext.getCurrentUserId();
                HoldSeatsCommand command = HoldSeatsCommand.builder()
                                .userId(userId)
                                .showtimeId(request.getShowtimeId())
                                .seatCodes(request.getSeatCodes())
                                .build();
                HoldSeatsResponse response = holdSeatsUseCase.execute(command);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Seats held successfully", response));
        }

        @PostMapping("/confirm")
        public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
                        @Valid @RequestBody ConfirmBookingRequest request) {
                Long userId = bookingContext.getCurrentUserId();
                ConfirmBookingCommand command = ConfirmBookingCommand.builder()
                                .userId(userId)
                                .holdToken(request.getHoldToken())
                                .paymentMethod(request.getPaymentMethod())
                                .build();
                BookingResponse response = confirmBookingUseCase.execute(command);
                return ResponseEntity.ok(ApiResponse.success("Booking confirmed successfully", response));
        }

        @PostMapping("/{bookingCode}/cancel")
        public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
                        @PathVariable String bookingCode,
                        @RequestBody(required = false) CancelBookingRequest request) {
                Long userId = bookingContext.getCurrentUserId();
                String reason = request != null ? request.getReason() : null;
                CancelBookingCommand command = CancelBookingCommand.builder()
                                .userId(userId)
                                .bookingCode(bookingCode)
                                .reason(reason)
                                .build();
                BookingResponse response = cancelBookingUseCase.execute(command);
                return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
        }

        @GetMapping("/{bookingCode}")
        public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable String bookingCode) {
                Booking booking = bookingRepository.findByBookingCode(bookingCode)
                                .orElseThrow(() -> new com.movieticket.bookingservice.api.exception.ApiException(
                                                com.movieticket.bookingservice.api.exception.ErrorCode.BOOKING_NOT_FOUND));
                List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());
                BookingResponse response = toBookingResponse(booking, tickets);
                return ResponseEntity.ok(ApiResponse.success(response));
        }

        @GetMapping("/my-bookings")
        public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings() {
                Long userId = bookingContext.getCurrentUserId();
                List<Booking> bookings = bookingRepository.findByUserId(userId);
                List<BookingResponse> responses = bookings.stream()
                                .map(b -> {
                                        List<Ticket> tickets = ticketRepository.findByBookingId(b.getId());
                                        return toBookingResponse(b, tickets);
                                })
                                .collect(Collectors.toList());
                return ResponseEntity.ok(ApiResponse.success(responses));
        }

        private BookingResponse toBookingResponse(Booking booking, List<Ticket> tickets) {
                List<BookingResponse.BookingSeatDto> seatDtos = booking.getSeats().stream()
                                .map(s -> BookingResponse.BookingSeatDto.builder()
                                                .seatCode(s.getSeatCode())
                                                .seatType(s.getSeatType())
                                                .price(s.getPrice())
                                                .status(s.getStatus().name())
                                                .build())
                                .collect(Collectors.toList());

                List<TicketResponse> ticketDtos = tickets.stream()
                                .map(t -> TicketResponse.builder()
                                                .id(t.getId())
                                                .ticketCode(t.getTicketCode())
                                                .bookingId(t.getBookingId())
                                                .userId(t.getUserId())
                                                .showtimeId(t.getShowtimeId())
                                                .movieId(t.getMovieId())
                                                .movieTitle(t.getMovieTitle())
                                                .cinemaId(t.getCinemaId())
                                                .cinemaName(t.getCinemaName())
                                                .hallId(t.getHallId())
                                                .hallName(t.getHallName())
                                                .seatCode(t.getSeatCode())
                                                .seatType(t.getSeatType())
                                                .showDate(t.getShowDate())
                                                .startTime(t.getStartTime())
                                                .endTime(t.getEndTime())
                                                .price(t.getPrice())
                                                .qrPayload(t.getQrPayload())
                                                .status(t.getStatus().name())
                                                .issuedAt(t.getIssuedAt())
                                                .build())
                                .collect(Collectors.toList());

                return BookingResponse.builder()
                                .id(booking.getId())
                                .bookingCode(booking.getBookingCode())
                                .userId(booking.getUserId())
                                .showtimeId(booking.getShowtimeId())
                                .totalAmount(booking.getTotalAmount())
                                .status(booking.getStatus().name())
                                .holdToken(booking.getHoldToken())
                                .seats(seatDtos)
                                .tickets(ticketDtos)
                                .createdAt(booking.getCreatedAt())
                                .updatedAt(booking.getUpdatedAt())
                                .build();
        }
}
