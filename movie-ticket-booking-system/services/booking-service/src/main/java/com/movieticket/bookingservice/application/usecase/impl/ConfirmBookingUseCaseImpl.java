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
import com.movieticket.bookingservice.domain.port.*;
import com.movieticket.bookingservice.domain.vo.BookingCode;
import com.movieticket.bookingservice.infrastructure.adapter.PaymentAdapter;
import com.movieticket.bookingservice.infrastructure.client.CinemaClient;
import com.movieticket.bookingservice.infrastructure.client.MovieClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfirmBookingUseCaseImpl implements ConfirmBookingUseCase {

    private final SeatHoldRepository seatHoldRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final PaymentRepository paymentRepository;
    private final BookingEventOutboxRepository outboxRepository;
    private final SagaTransactionRepository sagaTransactionRepository;
    private final PaymentAdapter paymentAdapter;
    private final MovieClient movieClient;
    private final CinemaClient cinemaClient;

    @Override
    @Transactional
    public BookingResponse execute(ConfirmBookingCommand command) {
        SeatHold seatHold = seatHoldRepository.findByHoldToken(command.getHoldToken())
                .orElseThrow(() -> new ApiException(ErrorCode.INVALID_HOLD_TOKEN,
                        "Hold token not found: " + command.getHoldToken()));

        if (!seatHold.isActive()) {
            throw new ApiException(ErrorCode.HOLD_EXPIRED,
                    "Seat hold has expired or is not active (status: " + seatHold.getStatus() + ")");
        }

        BookingCode bookingCode = BookingCode.generate();
        BigDecimal totalAmount = seatHold.getSeats().stream()
                .map(shSeat -> new BigDecimal("90000")) // tạm
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BookingSeat> bookingSeats = seatHold.getSeats().stream()
                .map(shSeat -> BookingSeat.builder()
                        .showtimeId(seatHold.getShowtimeId())
                        .seatCode(shSeat.getSeatCode())
                        .seatType("NORMAL")
                        .price(new BigDecimal("90000"))
                        .status(BookingSeatStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        Booking booking = Booking.builder()
                .bookingCode(bookingCode.value())
                .userId(command.getUserId())
                .showtimeId(seatHold.getShowtimeId())
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING_PAYMENT)
                .holdToken(command.getHoldToken())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seats(bookingSeats)
                .build();
        booking = bookingRepository.save(booking);

        final Long bookingId = booking.getId();
        booking.getSeats().forEach(s -> s.assignToBooking(bookingId));

        SagaTransaction saga = SagaTransaction.builder()
                .sagaId(UUID.randomUUID().toString())
                .bookingId(bookingId)
                .status(SagaStatus.STARTED)
                .currentStep("PAYMENT")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        saga = sagaTransactionRepository.save(saga);

        if ("VNPAY".equalsIgnoreCase(command.getPaymentMethod())) {
            Payment payment = Payment.builder()
                    .bookingId(bookingId)
                    .transactionRef("TXN" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase())
                    .method("VNPAY")
                    .amount(totalAmount)
                    .status(PaymentStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            payment = paymentRepository.save(payment);

            String ipAddress = command.getIpAddress() != null ? command.getIpAddress() : "127.0.0.1";
            String paymentUrl = paymentAdapter.createPaymentUrl(booking, payment, ipAddress);

            return BookingResponseMapper.toVnPayPendingResponse(booking, payment, paymentUrl);
        }

        Payment payment = paymentAdapter.processPayment(booking, command.getPaymentMethod());
        payment.assignToBooking(bookingId);
        payment = paymentRepository.save(payment);

        if (payment.getStatus() == PaymentStatus.PAID) {
            // Lấy thông tin showtime (tạm bỏ qua, dùng giá trị mặc định)
            Long movieId = null;
            Long cinemaId = null;
            Long hallId = null;
            String hallName = null;
            LocalDate showDate = LocalDate.now();
            LocalTime startTime = LocalTime.of(0, 0);
            LocalTime endTime = LocalTime.of(0, 0);

            // Gọi MovieService lấy thông tin phim
            final String[] movieTitle = {null};
            final String[] moviePosterUrl = {null};
            try {
                Map<String, Object> movieData = movieClient.getMovie(movieId); // cần movieId từ showtime
                movieTitle[0] = (String) movieData.get("title");
                moviePosterUrl[0] = (String) movieData.get("posterUrl");
            } catch (Exception e) {
                log.warn("Could not fetch movie details: {}", e.getMessage());
            }

            // Gọi CinemaService lấy thông tin rạp
            final String[] cinemaName = {null};
            try {
                Map<String, Object> cinemaData = cinemaClient.getCinema(cinemaId);
                cinemaName[0] = (String) cinemaData.get("name");
            } catch (Exception e) {
                log.warn("Could not fetch cinema details: {}", e.getMessage());
            }

            List<Ticket> tickets = bookingSeats.stream()
                    .map(bs -> {
                        String ticketCode = "TCK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
                        return Ticket.builder()
                                .ticketCode(ticketCode)
                                .bookingId(bookingId)
                                .userId(command.getUserId())
                                .showtimeId(seatHold.getShowtimeId())
                                .movieId(movieId)
                                .movieTitle(movieTitle[0])
                                .moviePosterUrl(moviePosterUrl[0])
                                .cinemaId(cinemaId)
                                .cinemaName(cinemaName[0])
                                .hallId(hallId)
                                .hallName(hallName)
                                .seatCode(bs.getSeatCode())
                                .seatType(bs.getSeatType())
                                .showDate(showDate)
                                .startTime(startTime)
                                .endTime(endTime)
                                .price(bs.getPrice())
                                .qrPayload("QR:" + ticketCode + ":" + bs.getSeatCode())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                    })
                    .collect(Collectors.toList());

            BookingAggregate aggregate = BookingAggregate.forNewConfirm(seatHold, booking, payment, saga);
            aggregate.confirmBooking(tickets);

            booking = bookingRepository.save(aggregate.getBooking());
            tickets = ticketRepository.saveAll(aggregate.getTickets());
            payment = paymentRepository.save(aggregate.getPayment());
            seatHoldRepository.save(aggregate.getSeatHold());
            sagaTransactionRepository.save(aggregate.getSaga());

            BookingEventOutbox outbox = BookingEventOutbox.builder()
                    .eventId(UUID.randomUUID().toString())
                    .aggregateType("Booking")
                    .aggregateId(bookingCode.value())
                    .bookingId(bookingId)
                    .eventType("BOOKING_CONFIRMED")
                    .topic("booking.confirmed")
                    .payloadJson("{\"bookingCode\":\"" + bookingCode.value()
                            + "\",\"userId\":" + command.getUserId()
                            + ",\"showtimeId\":" + seatHold.getShowtimeId()
                            + ",\"totalAmount\":" + totalAmount + "}")
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
            outboxRepository.save(outbox);

            return BookingResponseMapper.toResponse(booking, tickets, payment);
        } else {
            BookingAggregate aggregate = BookingAggregate.forNewConfirm(seatHold, booking, payment, saga);
            aggregate.compensateFailedPayment();

            bookingRepository.save(aggregate.getBooking());
            seatHoldRepository.save(aggregate.getSeatHold());
            sagaTransactionRepository.save(aggregate.getSaga());

            throw new ApiException(ErrorCode.PAYMENT_FAILED,
                    "Payment failed: " + (payment.getFailureReason() != null ? payment.getFailureReason() : "Unknown error"));
        }
    }

    @Override
    @Transactional
    public BookingResponse handleVnPayReturn(Map<String, String> vnpayParams) {
        if (!paymentAdapter.verifyReturn(vnpayParams)) {
            log.warn("VNPay return hash verification failed");
            throw new ApiException(ErrorCode.PAYMENT_FAILED, "VNPay hash verification failed");
        }

        String txnRef = vnpayParams.get("vnp_TxnRef");
        if (txnRef == null || txnRef.isBlank()) {
            throw new ApiException(ErrorCode.INVALID_REQUEST, "Missing vnp_TxnRef in VNPay return");
        }

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

        String responseCode = vnpayParams.get("vnp_ResponseCode");
        if ("00".equals(responseCode)) {
            final Long bkId = booking.getId();
            final Long bkUserId = booking.getUserId();
            final Long bkShowtimeId = booking.getShowtimeId();
            payment.markPaid(txnRef);
            booking.confirm();

            List<Ticket> tickets = booking.getSeats().stream()
                    .map(bs -> {
                        String ticketCode = "TCK" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
                        return Ticket.builder()
                                .ticketCode(ticketCode)
                                .bookingId(bkId)
                                .userId(bkUserId)
                                .showtimeId(bkShowtimeId)
                                .seatCode(bs.getSeatCode())
                                .seatType(bs.getSeatType())
                                .price(bs.getPrice())
                                .showDate(LocalDate.now())
                                .startTime(LocalTime.of(19, 0))
                                .endTime(LocalTime.of(21, 30))
                                .qrPayload("QR:" + ticketCode + ":" + bs.getSeatCode())
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                    })
                    .collect(Collectors.toList());
            tickets.forEach(Ticket::issue);

            booking = bookingRepository.save(booking);
            tickets = ticketRepository.saveAll(tickets);
            payment = paymentRepository.save(payment);

            seatHoldRepository.findByHoldToken(booking.getHoldToken()).ifPresent(sh -> {
                sh.convert();
                seatHoldRepository.save(sh);
            });

            sagaTransactionRepository.findByBookingId(booking.getId()).ifPresent(sg -> {
                sg.complete();
                sagaTransactionRepository.save(sg);
            });

            BookingEventOutbox outbox = BookingEventOutbox.builder()
                    .eventId(UUID.randomUUID().toString())
                    .aggregateType("Booking")
                    .aggregateId(booking.getBookingCode())
                    .bookingId(booking.getId())
                    .eventType("BOOKING_CONFIRMED")
                    .topic("booking.confirmed")
                    .payloadJson("{\"bookingCode\":\"" + booking.getBookingCode()
                            + "\",\"userId\":" + booking.getUserId()
                            + ",\"showtimeId\":" + booking.getShowtimeId()
                            + ",\"totalAmount\":" + booking.getTotalAmount() + "}")
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
            outboxRepository.save(outbox);

            return BookingResponseMapper.toResponse(booking, tickets, payment);
        } else {
            payment.markFailed("VNPay returned responseCode: " + responseCode);
            paymentRepository.save(payment);

            booking.fail("VNPay payment failed: responseCode=" + responseCode);
            bookingRepository.save(booking);

            seatHoldRepository.findByHoldToken(booking.getHoldToken()).ifPresent(sh -> {
                sh.release();
                seatHoldRepository.save(sh);
            });

            sagaTransactionRepository.findByBookingId(booking.getId()).ifPresent(sg -> {
                sg.fail("VNPay payment failed: responseCode=" + responseCode);
                sagaTransactionRepository.save(sg);
            });

            throw new ApiException(ErrorCode.PAYMENT_FAILED,
                    "VNPay payment failed with responseCode: " + responseCode);
        }
    }
}
