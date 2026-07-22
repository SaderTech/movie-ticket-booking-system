package com.movieticket.bookingservice.application.usecase.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.application.command.CancelBookingCommand;
import com.movieticket.bookingservice.application.mapper.BookingResponseMapper;
import com.movieticket.bookingservice.domain.aggregate.BookingAggregate;
import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.*;
import com.movieticket.bookingservice.infrastructure.jpa.*;
import com.movieticket.bookingservice.infrastructure.publisher.DomainEventPublisherImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CancelBookingUseCaseImpl {

    private final DomainEventPublisherImpl domainEventPublisher;
    private final JpaBookingRepository bookingRepository;
    private final JpaTicketRepository ticketRepository;
    private final JpaPaymentRepository paymentRepository;
    private final JpaSeatHoldRepository seatHoldRepository;
    private final JpaIdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public BookingResponse execute(CancelBookingCommand command) {
        String idempotencyKey = command.getIdempotencyKey();
        String requestHash = computeRequestHash(command);
        if (idempotencyKey != null) {
            Optional<BookingResponse> cached = checkIdempotency(idempotencyKey, requestHash);
            if (cached.isPresent()) {
                return cached.get();
            }
            try {
                IdempotencyRecord processingRecord = IdempotencyRecord.builder()
                        .idempotencyKey(idempotencyKey)
                        .requestHash(requestHash)
                        .operationType("CANCEL")
                        .status(IdempotencyStatus.PROCESSING)
                        .expiresAt(LocalDateTime.now().plusHours(1))
                        .build();
                idempotencyRecordRepository.saveAndFlush(processingRecord);
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                Optional<BookingResponse> rivalCached = checkIdempotency(idempotencyKey, requestHash);
                if (rivalCached.isPresent()) {
                    return rivalCached.get();
                }
            }
        }

        Booking booking = bookingRepository.findByBookingCode(command.getBookingCode())
                .orElseThrow(() -> new ApiException(ErrorCode.BOOKING_NOT_FOUND,
                        "Booking not found: " + command.getBookingCode()));

        if (!Objects.equals(booking.getUserId(), command.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, 403,
                    "You can only cancel your own bookings");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException(ErrorCode.BOOKING_CANNOT_BE_CANCELLED,
                    "Booking is already cancelled");
        }

        List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());
        Payment payment = paymentRepository.findByBookingId(booking.getId()).orElse(null);
        SeatHold seatHold = booking.getHoldToken() != null
                ? seatHoldRepository.findByHoldToken(booking.getHoldToken()).orElse(null)
                : null;

        BookingAggregate aggregate = BookingAggregate.forExistingCancel(booking, tickets, payment, seatHold);
        aggregate.cancelBooking(command.getReason());

        booking = bookingRepository.save(aggregate.getBooking());
        if (!aggregate.getTickets().isEmpty()) {
            ticketRepository.saveAll(aggregate.getTickets());
        }
        if (aggregate.getPayment() != null) {
            paymentRepository.save(aggregate.getPayment());
        }
        if (aggregate.getSeatHold() != null) {
            seatHoldRepository.save(aggregate.getSeatHold());
        }

        domainEventPublisher.publishAll(aggregate.getDomainEvents());
        aggregate.clearDomainEvents();

        BookingResponse response = BookingResponseMapper.toResponse(booking, tickets);

        if (idempotencyKey != null) {
            cacheIdempotencyResponse(idempotencyKey, response);
        }

        return response;
    }

    private Optional<BookingResponse> checkIdempotency(String idempotencyKey, String requestHash) {
        return idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)
                .flatMap(rec -> {
                    if (rec.getRequestHash() != null && !rec.getRequestHash().equals(requestHash)) {
                        throw new ApiException(ErrorCode.INVALID_REQUEST, 409,
                                "Idempotency key reused with different request payload");
                    }
                    switch (rec.getStatus()) {
                        case SUCCEEDED -> {
                            try {
                                return Optional.of(objectMapper.readValue(rec.getResponseBody(), BookingResponse.class));
                            } catch (Exception e) {
                                log.warn("Failed to deserialize cached idempotency response: {}", e.getMessage());
                            }
                        }
                        case PROCESSING -> throw new ApiException(ErrorCode.INVALID_REQUEST, 409, "Request is already being processed");
                        case FAILED -> {}
                    }
                    return Optional.empty();
                });
    }

    private void cacheIdempotencyResponse(String idempotencyKey, BookingResponse response) {
        idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey).ifPresent(rec -> {
            try {
                rec.succeed(objectMapper.writeValueAsString(response));
                idempotencyRecordRepository.save(rec);
            } catch (Exception e) {
                log.warn("Failed to cache idempotency response: {}", e.getMessage());
            }
        });
    }

    private String computeRequestHash(CancelBookingCommand command) {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("bookingCode", command.getBookingCode());
            data.put("reason", command.getReason());
            String json = objectMapper.writeValueAsString(data);
            return sha256(json);
        } catch (Exception e) {
            log.warn("Failed to compute request hash: {}", e.getMessage());
            return null;
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}