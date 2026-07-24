package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.application.command.ConfirmBookingCommand;
import com.movieticket.bookingservice.application.mapper.BookingResponseMapper;
import com.movieticket.bookingservice.application.usecase.ConfirmBookingUseCase;
import com.movieticket.bookingservice.domain.aggregate.BookingAggregate;
import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.*;
import com.movieticket.bookingservice.domain.event.BookingCancelledEvent;
import com.movieticket.bookingservice.domain.event.PaymentRefundRequiredEvent;
import com.movieticket.bookingservice.domain.vo.BookingCode;
import com.movieticket.bookingservice.domain.repository.*;
import com.movieticket.bookingservice.infrastructure.adapter.VnPayUtil;
import com.movieticket.bookingservice.infrastructure.adapter.PaymentAdapter;
import com.movieticket.bookingservice.infrastructure.client.CinemaClient;
import com.movieticket.bookingservice.infrastructure.client.MovieClient;
import com.movieticket.bookingservice.infrastructure.client.SeatClient;
import com.movieticket.bookingservice.infrastructure.client.ShowtimeClient;
import com.movieticket.bookingservice.infrastructure.client.dto.CinemaResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.MovieResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.SeatResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.ShowtimeResponse;
import com.movieticket.bookingservice.infrastructure.publisher.DomainEventPublisherImpl;
import com.movieticket.bookingservice.infrastructure.security.BookingContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Optional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmBookingUseCaseImpl implements ConfirmBookingUseCase {

    private final SeatHoldRepository seatHoldRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final SagaTransactionRepository sagaTransactionRepository;
    private final DomainEventPublisherImpl domainEventPublisher;
    private final PaymentAdapter paymentAdapter;
    private final BookingSettingRepository bookingSettingRepository;
    private final ShowtimeClient showtimeClient;
    private final MovieClient movieClient;
    private final CinemaClient cinemaClient;
    private final SeatClient seatClient;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final BookingContext bookingContext;

    private static final String SETTING_LOCK_WAIT = "lock_wait_time_seconds";
    private static final String SETTING_LOCK_LEASE = "lock_lease_time_seconds";
    private static final String SETTING_HOLD_PAYMENT_EXTENSION = "hold_payment_extension_minutes";
    private static final int DEFAULT_LOCK_WAIT = 2;
    private static final int DEFAULT_LOCK_LEASE = 10;
    private static final int DEFAULT_HOLD_PAYMENT_EXTENSION = 30;

    @Lazy
    @Autowired
    private ConfirmBookingUseCaseImpl self;

    public BookingResponse execute(ConfirmBookingCommand command) {
        RLock lock = redissonClient.getLock("lock:hold:" + command.getHoldToken());
        int lockWait = getIntSetting(SETTING_LOCK_WAIT, DEFAULT_LOCK_WAIT);
        int lockLease = getIntSetting(SETTING_LOCK_LEASE, DEFAULT_LOCK_LEASE);
        try {
            boolean acquired = lock.tryLock(lockWait, lockLease, TimeUnit.SECONDS);
            if (!acquired) {
                throw new ApiException(ErrorCode.BOOKING_CONFLICT, 409,
                        "Another transaction is processing this hold token: " + command.getHoldToken());
            }
            return self.doExecute(command);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Lock acquisition interrupted");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                try { lock.unlock(); } catch (Exception e) { log.warn("Error releasing hold lock: {}", e.getMessage()); }
            }
        }
    }

    @Transactional
    protected BookingResponse doExecute(ConfirmBookingCommand command) {
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
                        .operationType("CONFIRM")
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

        SeatHold seatHold = seatHoldRepository.findByHoldToken(command.getHoldToken())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_HOLD_TOKEN,
                        "Hold token not found: " + command.getHoldToken()));

        if (!Objects.equals(seatHold.getUserId(), command.getUserId())) {
            throw new ApiException(ErrorCode.FORBIDDEN, 403,
                    "Hold token does not belong to the current user");
        }

        if (!seatHold.isActive()) {
            throw new ApiException(ErrorCode.HOLD_EXPIRED,
                    "Seat hold has expired or is not active (status: " + seatHold.getStatus() + ")");
        }

        ShowtimeResponse showtime = getShowtimeForPricing(seatHold.getShowtimeId());
        BookingCode bookingCode = BookingCode.generate();
        BigDecimal ticketPrice = showtime.price();
        BigDecimal totalAmount = seatHold.getSeats().stream()
                .map(shSeat -> ticketPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, String> seatTypeMap = fetchSeatTypeMap(seatHold.getShowtimeId());

        List<BookingSeat> bookingSeats = seatHold.getSeats().stream()
                .map(shSeat -> {
                    String seatType = seatTypeMap.getOrDefault(shSeat.getSeatCode(), "NORMAL");
                    return BookingSeat.builder()
                            .showtimeId(seatHold.getShowtimeId())
                            .seatCode(shSeat.getSeatCode())
                            .seatType(seatType)
                            .price(ticketPrice)
                            .status(BookingSeatStatus.PENDING)
                            .createdAt(LocalDateTime.now())
                            .build();
                })
                .collect(Collectors.toList());

        String customerEmail = bookingContext != null ? bookingContext.getCurrentUserEmail() : null;
        String customerName = displayNameFromEmail(customerEmail);

        Booking booking = Booking.builder()
                .bookingCode(bookingCode.value())
                .userId(command.getUserId())
                .customerEmail(customerEmail)
                .customerName(customerName)
                .showtimeId(seatHold.getShowtimeId())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING_PAYMENT)
                .holdToken(command.getHoldToken())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seats(bookingSeats)
                .build();
        for (BookingSeat seat : bookingSeats) {
            seat.setBooking(booking);
        }
        booking = bookingRepository.save(booking);

        SagaTransaction saga = SagaTransaction.builder()
                .sagaId(UUID.randomUUID().toString())
                .bookingId(booking.getId())
                .status(SagaStatus.STARTED)
                .currentStep("PAYMENT")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        saga = sagaTransactionRepository.save(saga);

        if (!"VNPAY".equalsIgnoreCase(command.getPaymentMethod())) {
            throw new ApiException(ErrorCode.INVALID_REQUEST,
                    "Only VNPAY payment method is supported");
        }

        Payment payment = Payment.builder()
                .bookingId(booking.getId())
                .transactionRef("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase())
                .method("VNPAY")
                .amount(totalAmount)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        payment = paymentRepository.save(payment);

        String ipAddress = command.getIpAddress() != null ? command.getIpAddress() : "127.0.0.1";
            String paymentUrl = paymentAdapter.createPaymentUrl(booking, payment, ipAddress, command.getReturnUrl());

        int holdPaymentExtension = getIntSetting(SETTING_HOLD_PAYMENT_EXTENSION, DEFAULT_HOLD_PAYMENT_EXTENSION);
        seatHold.extendExpiry(holdPaymentExtension);
        seatHoldRepository.save(seatHold);
        log.info("Extended hold {} expiry by {} minutes for VNPay payment window (new expiresAt: {})",
                seatHold.getHoldToken(), holdPaymentExtension, seatHold.getExpiresAt());

        BookingResponse response = BookingResponseMapper.toVnPayPendingResponse(booking, payment, paymentUrl);
        cacheIdempotencyResponse(idempotencyKey, response);
        return response;
    }

    private ShowtimeResponse getShowtimeForPricing(Long showtimeId) {
        try {
            ShowtimeResponse showtime = showtimeClient.getShowtime(showtimeId);
            if (showtime == null || showtime.price() == null || showtime.price().signum() <= 0) {
                throw new ApiException(ErrorCode.INVALID_REQUEST,
                        "Showtime " + showtimeId + " does not have a valid ticket price");
            }
            return showtime;
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Could not load ticket price for showtime {}: {}", showtimeId, e.getMessage());
            throw new ApiException(ErrorCode.INTERNAL_ERROR,
                    "Could not determine ticket price for the selected showtime");
        }
    }

    public BookingResponse handleVnPayReturn(Map<String, String> vnpayParams) {
        if (!paymentAdapter.verifyReturn(vnpayParams)) {
            log.warn("VNPay return hash verification failed");
            throw new ApiException(ErrorCode.PAYMENT_FAILED, "VNPay hash verification failed");
        }

        String txnRef = vnpayParams.get("vnp_TxnRef");
        if (txnRef == null || txnRef.isBlank()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Missing vnp_TxnRef in VNPay return");
        }

        RLock lock = redissonClient.getLock("lock:vnpay:" + txnRef);
        int lockWait = getIntSetting(SETTING_LOCK_WAIT, DEFAULT_LOCK_WAIT);
        int lockLease = getIntSetting(SETTING_LOCK_LEASE, DEFAULT_LOCK_LEASE);
        try {
            boolean acquired = lock.tryLock(lockWait, lockLease, TimeUnit.SECONDS);
            if (!acquired) {
                throw new ApiException(ErrorCode.INTERNAL_ERROR, "Could not acquire lock for VNPay return processing");
            }
            return self.doHandleVnPayReturn(vnpayParams, txnRef);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "Lock acquisition interrupted");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                try { lock.unlock(); } catch (Exception e) { log.warn("Error releasing VNPay lock: {}", e.getMessage()); }
            }
        }
    }

    @Transactional
    protected BookingResponse doHandleVnPayReturn(Map<String, String> vnpayParams, String txnRef) {
        Payment payment = paymentRepository.findByTransactionRef(txnRef)
                .orElseThrow(() -> new ApiException(ErrorCode.PAYMENT_FAILED,
                        "Payment not found for transaction: " + txnRef));

        if (payment.getStatus() == PaymentStatus.PAID) {
            log.info("Payment {} already processed, returning idempotent response", txnRef);
            Booking booking = bookingRepository.findById(payment.getBookingId())
                    .orElseThrow(() -> new ApiException(ErrorCode.BOOKING_NOT_FOUND,
                            "Booking not found for payment: " + txnRef));
            List<Ticket> tickets = ticketRepository.findByBookingId(booking.getId());
            return BookingResponseMapper.toResponse(booking, tickets, payment);
        }

        Booking booking = bookingRepository.findById(payment.getBookingId())
                .orElseThrow(() -> new ApiException(ErrorCode.BOOKING_NOT_FOUND,
                        "Booking not found for payment: " + txnRef));

        if (payment.getStatus() == PaymentStatus.REFUND_PENDING) {
            log.info("Late payment {} was already marked for refund", txnRef);
            return BookingResponseMapper.toResponse(booking, List.of(), payment);
        }

        try {
            String rawJson = objectMapper.writeValueAsString(vnpayParams);
            payment.setVnPayDetails(vnpayParams.get("vnp_TransactionNo"), rawJson);
        } catch (Exception e) {
            log.warn("Failed to serialize VNPay response: {}", e.getMessage());
        }

        String responseCode = vnpayParams.get("vnp_ResponseCode");
        String transactionStatus = vnpayParams.get("vnp_TransactionStatus");

        log.info("VNPay return: txnRef={}, responseCode={} ({}), transactionStatus={}",
                txnRef, responseCode, VnPayUtil.getResponseCodeDescription(responseCode), transactionStatus);

        if (transactionStatus != null && !"00".equals(transactionStatus)) {
            log.warn("VNPay transaction status indicates failure: {} ({})",
                    transactionStatus, VnPayUtil.getResponseCodeDescription(transactionStatus));
        }

        String vnpAmountStr = vnpayParams.get("vnp_Amount");
        if (vnpAmountStr != null) {
            try {
                long expectedAmount = payment.getAmount().multiply(java.math.BigDecimal.valueOf(100)).longValue();
                long vnpAmount = Long.parseLong(vnpAmountStr);
                if (vnpAmount != expectedAmount) {
                    log.error("VNPay amount mismatch: expected {}, got {}", expectedAmount, vnpAmount);
                    throw new ApiException(ErrorCode.PAYMENT_FAILED,
                            "VNPay amount mismatch: expected " + expectedAmount + ", got " + vnpAmount);
                }
            } catch (NumberFormatException e) {
                log.warn("Invalid vnp_Amount format: {}", vnpAmountStr);
            }
        }

        if ("00".equals(responseCode)) {
            String bookingHoldToken = booking.getHoldToken();
            SeatHold currentHold = seatHoldRepository.findByHoldToken(bookingHoldToken)
                    .orElseThrow(() -> new ApiException(ErrorCode.HOLD_NOT_FOUND,
                            "Seat hold not found: " + bookingHoldToken));
            if (!currentHold.isActive()) {
                return compensateLatePayment(payment, booking, currentHold, txnRef);
            }

            final Long bkId = booking.getId();
            final Long bkUserId = booking.getUserId();
            final Long bkShowtimeId = booking.getShowtimeId();

            Long movieId = null;
            Long cinemaId = null;
            Long hallId = null;
            // Showtime only supplies the room id.  Tickets.hall_name is NOT NULL in
            // the database, so always provide a stable display value even when the
            // cinema service cannot be reached to enrich the room details.
            String hallName = "Phòng chiếu";
            LocalDate showDate;
            LocalTime startTime;
            LocalTime endTime;
            try {
            ShowtimeResponse showtimeData = showtimeClient.getShowtime(bkShowtimeId);
                movieId = showtimeData.movieId();
                cinemaId = showtimeData.cinemaId();
                hallId = showtimeData.roomId();
                if (hallId != null) {
                    hallName = "Phòng chiếu " + hallId;
                }
                showDate = showtimeData.showDate();
                startTime = showtimeData.startTime();
                endTime = showtimeData.endTime();
            } catch (Exception e) {
                log.warn("Could not fetch showtime details, using defaults: {}", e.getMessage());
                showDate = LocalDate.now();
                startTime = LocalTime.of(19, 0);
                endTime = LocalTime.of(21, 30);
            }

            String movieTitle = null;
            String moviePosterUrl = null;
            if (movieId != null) {
                try {
            MovieResponse movieData = movieClient.getMovie(movieId);
                    movieTitle = movieData.title();
                    moviePosterUrl = movieData.posterUrl();
                } catch (Exception e) {
                    log.warn("Could not fetch movie details: {}", e.getMessage());
                }
            }

            String cinemaName = null;
            if (cinemaId != null) {
                try {
            CinemaResponse cinemaData = cinemaClient.getCinema(cinemaId);
                    cinemaName = cinemaData.name();
                } catch (Exception e) {
                    log.warn("Could not fetch cinema details: {}", e.getMessage());
                }
            }

            final LocalDate finalShowDate = showDate;
            final LocalTime finalStartTime = startTime;
            final LocalTime finalEndTime = endTime;
            final String finalMovieTitle = movieTitle;
            final String finalMoviePosterUrl = moviePosterUrl;
            final String finalCinemaName = cinemaName;
            final Long finalMovieId = movieId;
            final Long finalCinemaId = cinemaId;
            final Long finalHallId = hallId;
            final String finalHallName = hallName;

            String holdToken = booking.getHoldToken();
            Long bookingIdLocal = booking.getId();
            SeatHold seatHold = seatHoldRepository.findByHoldToken(holdToken)
                    .orElseThrow(() -> new ApiException(ErrorCode.HOLD_NOT_FOUND,
                            "Seat hold not found: " + holdToken));
            SagaTransaction saga = sagaTransactionRepository.findByBookingId(bookingIdLocal)
                    .orElseThrow(() -> new ApiException(ErrorCode.INTERNAL_ERROR,
                            "Saga not found for booking: " + bookingIdLocal));

            List<Ticket> tickets = booking.getSeats().stream()
                    .map(bs -> {
                        String ticketCode = "TCK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
                        return Ticket.builder()
                                .ticketCode(ticketCode)
                                .bookingId(bkId)
                                .userId(bkUserId)
                                .showtimeId(bkShowtimeId)
                                .movieId(finalMovieId)
                                .movieTitle(finalMovieTitle)
                                .moviePosterUrl(finalMoviePosterUrl)
                                .cinemaId(finalCinemaId)
                                .cinemaName(finalCinemaName)
                                .hallId(finalHallId)
                                .hallName(finalHallName)
                                .seatCode(bs.getSeatCode())
                                .seatType(bs.getSeatType())
                                .price(bs.getPrice())
                                .showDate(finalShowDate)
                                .startTime(finalStartTime)
                                .endTime(finalEndTime)
                                .qrPayload("QR:" + ticketCode + ":" + bs.getSeatCode())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                    })
                    .collect(Collectors.toList());

            BookingAggregate aggregate = BookingAggregate.forNewConfirm(seatHold, booking, payment, saga);
            aggregate.confirmBooking(tickets);

            try {
                booking = bookingRepository.save(aggregate.getBooking());
                tickets = ticketRepository.saveAll(aggregate.getTickets());
                payment = paymentRepository.save(aggregate.getPayment());
                seatHoldRepository.save(aggregate.getSeatHold());
                sagaTransactionRepository.save(aggregate.getSaga());
            } catch (DataIntegrityViolationException e) {
                log.warn("Booking conflict detected for hold token {}: {}", holdToken, e.getMessage());
                Booking freshBooking = bookingRepository.findById(bookingIdLocal).orElse(null);
                Payment freshPayment = paymentRepository.findByTransactionRef(txnRef).orElse(null);
                if (freshPayment != null && freshPayment.getStatus() != PaymentStatus.FAILED) {
                    freshPayment.markFailed("Double booking detected: " + e.getMessage());
                }
                SeatHold freshSeatHold = seatHoldRepository.findByHoldToken(holdToken).orElse(null);
                SagaTransaction freshSaga = sagaTransactionRepository.findByBookingId(bookingIdLocal).orElse(null);
                BookingAggregate compensationAgg = BookingAggregate.forNewConfirm(freshSeatHold, freshBooking, freshPayment, freshSaga);
                compensationAgg.compensateFailedPayment("Double booking detected: " + e.getMessage());
                if (compensationAgg.getPayment() != null) {
                    paymentRepository.save(compensationAgg.getPayment());
                }
                if (compensationAgg.getBooking() != null) {
                    bookingRepository.save(compensationAgg.getBooking());
                }
                if (compensationAgg.getSeatHold() != null) {
                    seatHoldRepository.save(compensationAgg.getSeatHold());
                }
                if (compensationAgg.getSaga() != null) {
                    sagaTransactionRepository.save(compensationAgg.getSaga());
                }
                domainEventPublisher.publishAll(compensationAgg.getDomainEvents());
                compensationAgg.clearDomainEvents();
                throw new ApiException(ErrorCode.BOOKING_CONFLICT, 409, "Seat already booked by another user");
            }

            domainEventPublisher.publishAll(aggregate.getDomainEvents());
            aggregate.clearDomainEvents();

            return BookingResponseMapper.toResponse(booking, tickets, payment);
        } else {
            String errorDesc = VnPayUtil.getResponseCodeDescription(responseCode);
            payment.markFailed("VNPay returned responseCode: " + responseCode + " - " + errorDesc);

            SeatHold seatHold = seatHoldRepository.findByHoldToken(booking.getHoldToken()).orElse(null);
            SagaTransaction saga = sagaTransactionRepository.findByBookingId(booking.getId()).orElse(null);

            BookingAggregate aggregate = BookingAggregate.forNewConfirm(seatHold, booking, payment, saga);
            aggregate.compensateFailedPayment("VNPay payment failed: responseCode=" + responseCode + " - " + errorDesc);

            paymentRepository.save(aggregate.getPayment());
            bookingRepository.save(aggregate.getBooking());
            if (aggregate.getSeatHold() != null) {
                seatHoldRepository.save(aggregate.getSeatHold());
            }
            if (aggregate.getSaga() != null) {
                sagaTransactionRepository.save(aggregate.getSaga());
            }

            domainEventPublisher.publishAll(aggregate.getDomainEvents());
            aggregate.clearDomainEvents();

            // Do not throw here: ApiException is a runtime exception and would
            // roll back the compensation that has just released the hold.
            return BookingResponseMapper.toResponse(booking, List.of(), payment);
        }
    }

    private BookingResponse compensateLatePayment(Payment payment, Booking booking, SeatHold seatHold, String txnRef) {
        String reason = "Payment received after seat hold expiration";
        payment.markRefundPending(reason);
        booking.fail(reason);
        seatHold.release();

        SagaTransaction saga = sagaTransactionRepository.findByBookingId(booking.getId()).orElse(null);
        if (saga != null) {
            saga.startCompensation();
            saga.compensate();
            sagaTransactionRepository.save(saga);
        }

        paymentRepository.save(payment);
        bookingRepository.save(booking);
        seatHoldRepository.save(seatHold);
        domainEventPublisher.publishAll(List.of(
                new BookingCancelledEvent(booking.getBookingCode(), booking.getUserId(), reason),
                new PaymentRefundRequiredEvent(txnRef, booking.getBookingCode(), booking.getUserId(),
                        booking.getCustomerEmail(), booking.getCustomerName(), payment.getAmount(), reason)
        ));

        log.warn("Late VNPay payment {} marked REFUND_PENDING for booking {}", txnRef, booking.getBookingCode());
        return BookingResponseMapper.toResponse(booking, List.of(), payment);
    }

    private String displayNameFromEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private Map<String, String> fetchSeatTypeMap(Long showtimeId) {
        try {
            ShowtimeResponse showtimeData = showtimeClient.getShowtime(showtimeId);
            Long hallId = showtimeData.roomId();
            if (hallId == null) {
                log.warn("No hallId in showtime data, falling back to NORMAL seat type");
                return Map.of();
            }
            List<SeatResponse> seats = seatClient.getSeatsByHallId(hallId);
            Map<String, String> map = new java.util.HashMap<>();
            for (SeatResponse seat : seats) {
                if (seat.rowName() != null && seat.seatNumber() != null && seat.seatTypeCode() != null) {
                    map.put(seat.rowName() + seat.seatNumber(), seat.seatTypeCode());
                }
            }
            log.debug("Fetched seat type map for hall {}: {} entries", hallId, map.size());
            return map;
        } catch (Exception e) {
            log.warn("Could not fetch seat types from cinema service, falling back to NORMAL: {}", e.getMessage());
            return Map.of();
        }
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
        if (idempotencyKey == null) return;
        idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey).ifPresent(rec -> {
            try {
                rec.succeed(objectMapper.writeValueAsString(response));
                idempotencyRecordRepository.save(rec);
            } catch (Exception e) {
                log.warn("Failed to cache idempotency response: {}", e.getMessage());
            }
        });
    }

    private String computeRequestHash(ConfirmBookingCommand command) {
        try {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("holdToken", command.getHoldToken());
            data.put("paymentMethod", command.getPaymentMethod());
            data.put("returnUrl", command.getReturnUrl());
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

    private int getIntSetting(String key, int defaultValue) {
        try {
            return bookingSettingRepository.findBySettingKey(key)
                    .map(setting -> {
                        try {
                            return Integer.parseInt(setting.getSettingValue());
                        } catch (NumberFormatException e) {
                            return defaultValue;
                        }
                    })
                    .orElse(defaultValue);
        } catch (Exception e) {
            log.warn("Could not read setting {}, using default {}: {}", key, defaultValue, e.getMessage());
            return defaultValue;
        }
    }
}
