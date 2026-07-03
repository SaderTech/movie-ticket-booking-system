package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.application.command.ConfirmBookingCommand;
import com.movieticket.bookingservice.domain.aggregate.BookingAggregate;
import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.*;
import com.movieticket.bookingservice.domain.port.*;
import com.movieticket.bookingservice.infrastructure.adapter.PaymentAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmBookingUseCaseImplTest {

    @Mock private SeatHoldRepository seatHoldRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private BookingEventOutboxRepository outboxRepository;
    @Mock private SagaTransactionRepository sagaTransactionRepository;
    @Mock private PaymentAdapter paymentAdapter;

    @InjectMocks
    private ConfirmBookingUseCaseImpl useCase;

    private SeatHold activeHold;
    private ConfirmBookingCommand mockCommand;
    private ConfirmBookingCommand vnpayCommand;

    @BeforeEach
    void setUp() {
        SeatHoldSeat seat = SeatHoldSeat.builder()
                .showtimeId(1L)
                .seatCode("A1")
                .build();

        activeHold = SeatHold.builder()
                .id(1L)
                .holdToken("HOLD_TEST123")
                .userId(1L)
                .showtimeId(1L)
                .status(SeatHoldStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seats(List.of(seat))
                .build();

        mockCommand = ConfirmBookingCommand.builder()
                .userId(1L)
                .holdToken("HOLD_TEST123")
                .paymentMethod("MOCK")
                .build();

        vnpayCommand = ConfirmBookingCommand.builder()
                .userId(1L)
                .holdToken("HOLD_TEST123")
                .paymentMethod("VNPAY")
                .ipAddress("127.0.0.1")
                .build();
    }

    @Test
    void execute_HoldExpired_ThrowsException() {
        SeatHold expiredHold = SeatHold.builder()
                .id(1L)
                .holdToken("HOLD_EXPIRED")
                .status(SeatHoldStatus.EXPIRED)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seats(List.of())
                .build();
        when(seatHoldRepository.findByHoldToken("HOLD_EXPIRED")).thenReturn(Optional.of(expiredHold));

        ConfirmBookingCommand cmd = ConfirmBookingCommand.builder()
                .userId(1L).holdToken("HOLD_EXPIRED").paymentMethod("MOCK").build();

        assertThrows(ApiException.class, () -> useCase.execute(cmd));
    }

    // ==================== MOCK FLOW ====================

    @Test
    void execute_MockFlow_Success() {
        when(seatHoldRepository.findByHoldToken("HOLD_TEST123")).thenReturn(Optional.of(activeHold));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            return Booking.builder()
                    .id(1L)
                    .bookingCode(b.getBookingCode())
                    .userId(b.getUserId())
                    .showtimeId(b.getShowtimeId())
                    .totalAmount(b.getTotalAmount())
                    .status(b.getStatus())
                    .holdToken(b.getHoldToken())
                    .createdAt(b.getCreatedAt())
                    .updatedAt(b.getUpdatedAt())
                    .seats(b.getSeats())
                    .build();
        });

        Payment paidPayment = Payment.builder()
                .id(1L)
                .bookingId(1L)
                .transactionRef("TXN_MOCK123")
                .method("MOCK")
                .amount(BigDecimal.ZERO)
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(paymentAdapter.processPayment(any(Booking.class), eq("MOCK"))).thenReturn(paidPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(paidPayment);
        when(ticketRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(activeHold);
        when(sagaTransactionRepository.save(any(SagaTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(BookingEventOutbox.class))).thenReturn(null);

        BookingResponse response = useCase.execute(mockCommand);

        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());
        assertNotNull(response.getTickets());
        assertEquals(1, response.getTickets().size());
        verify(paymentAdapter, times(1)).processPayment(any(Booking.class), eq("MOCK"));
    }

    @Test
    void execute_MockFlow_PaymentFailed() {
        when(seatHoldRepository.findByHoldToken("HOLD_TEST123")).thenReturn(Optional.of(activeHold));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            return Booking.builder()
                    .id(1L)
                    .bookingCode(b.getBookingCode())
                    .userId(b.getUserId())
                    .showtimeId(b.getShowtimeId())
                    .totalAmount(b.getTotalAmount())
                    .status(b.getStatus())
                    .holdToken(b.getHoldToken())
                    .createdAt(b.getCreatedAt())
                    .updatedAt(b.getUpdatedAt())
                    .seats(b.getSeats())
                    .build();
        });
        when(sagaTransactionRepository.save(any(SagaTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment failedPayment = Payment.builder()
                .id(1L)
                .bookingId(1L)
                .transactionRef("TXN_MOCK_FAIL")
                .method("MOCK")
                .amount(BigDecimal.ZERO)
                .status(PaymentStatus.FAILED)
                .failureReason("Insufficient funds")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(paymentAdapter.processPayment(any(Booking.class), eq("MOCK"))).thenReturn(failedPayment);
        when(paymentRepository.save(any(Payment.class))).thenReturn(failedPayment);
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(null);

        assertThrows(ApiException.class, () -> useCase.execute(mockCommand));
    }

    // ==================== VNPAY FLOW - PHASE 1 ====================

    @Test
    void execute_VnPayFlow_Phase1_ReturnsPaymentUrl() {
        when(seatHoldRepository.findByHoldToken("HOLD_TEST123")).thenReturn(Optional.of(activeHold));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            return Booking.builder()
                    .id(1L)
                    .bookingCode(b.getBookingCode())
                    .userId(b.getUserId())
                    .showtimeId(b.getShowtimeId())
                    .totalAmount(b.getTotalAmount())
                    .status(b.getStatus())
                    .holdToken(b.getHoldToken())
                    .createdAt(b.getCreatedAt())
                    .updatedAt(b.getUpdatedAt())
                    .seats(b.getSeats())
                    .build();
        });
        when(sagaTransactionRepository.save(any(SagaTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentAdapter.createPaymentUrl(any(Booking.class), any(Payment.class), eq("127.0.0.1")))
                .thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=TEST&vnp_SecureHash=abc");

        BookingResponse response = useCase.execute(vnpayCommand);

        assertNotNull(response);
        assertEquals("PENDING_PAYMENT", response.getStatus());
        assertNotNull(response.getPaymentUrl());
        assertTrue(response.getPaymentUrl().startsWith("https://sandbox.vnpayment.vn"));
        assertTrue(response.getTickets().isEmpty());
        verify(paymentAdapter, never()).processPayment(any(), any());
    }

    // ==================== VNPAY FLOW - PHASE 2 (CALLBACK) ====================

    @Test
    void handleVnPayReturn_Success() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "TXN_VNPAY123");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_SecureHash", "valid");

        when(paymentAdapter.verifyReturn(params)).thenReturn(true);

        Payment payment = Payment.builder()
                .id(1L)
                .bookingId(1L)
                .transactionRef("TXN_VNPAY123")
                .method("VNPAY")
                .amount(BigDecimal.ZERO)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(paymentRepository.findByTransactionRef("TXN_VNPAY123")).thenReturn(Optional.of(payment));

        BookingSeat seat = BookingSeat.builder()
                .seatCode("A1")
                .price(BigDecimal.ZERO)
                .status(BookingSeatStatus.PENDING)
                .build();
        Booking booking = Booking.builder()
                .id(1L)
                .bookingCode("BK_TEST")
                .userId(1L)
                .showtimeId(1L)
                .totalAmount(BigDecimal.ZERO)
                .status(BookingStatus.PENDING_PAYMENT)
                .holdToken("HOLD_TEST123")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seats(List.of(seat))
                .build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(BookingEventOutbox.class))).thenReturn(null);
        when(seatHoldRepository.findByHoldToken("HOLD_TEST123")).thenReturn(Optional.of(activeHold));
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(null);
        when(sagaTransactionRepository.findByBookingId(1L)).thenReturn(Optional.empty());

        BookingResponse response = useCase.handleVnPayReturn(params);

        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());
        assertEquals(PaymentStatus.PAID, payment.getStatus());
        verify(ticketRepository, times(1)).saveAll(anyList());
    }

    @Test
    void handleVnPayReturn_Idempotent() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "TXN_ALREADY_PAID");
        params.put("vnp_SecureHash", "valid");

        when(paymentAdapter.verifyReturn(params)).thenReturn(true);

        Payment payment = Payment.builder()
                .id(1L)
                .bookingId(1L)
                .transactionRef("TXN_ALREADY_PAID")
                .method("VNPAY")
                .amount(BigDecimal.ZERO)
                .status(PaymentStatus.PAID)
                .paidAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(paymentRepository.findByTransactionRef("TXN_ALREADY_PAID")).thenReturn(Optional.of(payment));

        Booking booking = Booking.builder()
                .id(1L)
                .bookingCode("BK_DONE")
                .userId(1L)
                .showtimeId(1L)
                .totalAmount(BigDecimal.ZERO)
                .status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seats(List.of())
                .build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(ticketRepository.findByBookingId(1L)).thenReturn(List.of());

        BookingResponse response = useCase.handleVnPayReturn(params);

        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());
        verify(ticketRepository, never()).saveAll(anyList());
        verify(paymentRepository, never()).save(any());
    }

    @Test
    void handleVnPayReturn_FailedPayment() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "TXN_FAIL");
        params.put("vnp_ResponseCode", "99");
        params.put("vnp_SecureHash", "valid");

        when(paymentAdapter.verifyReturn(params)).thenReturn(true);

        Payment payment = Payment.builder()
                .id(1L)
                .bookingId(1L)
                .transactionRef("TXN_FAIL")
                .method("VNPAY")
                .amount(BigDecimal.ZERO)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(paymentRepository.findByTransactionRef("TXN_FAIL")).thenReturn(Optional.of(payment));

        BookingSeat seat = BookingSeat.builder().seatCode("A1").build();
        Booking booking = Booking.builder()
                .id(1L)
                .bookingCode("BK_FAIL")
                .userId(1L)
                .showtimeId(1L)
                .totalAmount(BigDecimal.ZERO)
                .status(BookingStatus.PENDING_PAYMENT)
                .holdToken("HOLD_TEST")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seats(List.of(seat))
                .build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThrows(ApiException.class, () -> useCase.handleVnPayReturn(params));
        assertEquals(PaymentStatus.FAILED, payment.getStatus());
    }

    @Test
    void handleVnPayReturn_InvalidHash() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "TXN_BAD_HASH");
        params.put("vnp_SecureHash", "invalid");

        when(paymentAdapter.verifyReturn(params)).thenReturn(false);

        assertThrows(ApiException.class, () -> useCase.handleVnPayReturn(params));
        verify(paymentRepository, never()).findByTransactionRef(any());
    }

    @Test
    void handleVnPayReturn_MissingTxnRef_ThrowsException() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_SecureHash", "valid");

        when(paymentAdapter.verifyReturn(params)).thenReturn(true);

        assertThrows(ApiException.class, () -> useCase.handleVnPayReturn(params));
    }

    @Test
    void handleVnPayReturn_PaymentNotFound_ThrowsException() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "TXN_NOT_FOUND");
        params.put("vnp_SecureHash", "valid");

        when(paymentAdapter.verifyReturn(params)).thenReturn(true);
        when(paymentRepository.findByTransactionRef("TXN_NOT_FOUND")).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> useCase.handleVnPayReturn(params));
    }

    @Test
    void handleVnPayReturn_Success_WithSagaAndSeatHoldPresent() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_TxnRef", "TXN_VNPAY456");
        params.put("vnp_ResponseCode", "00");
        params.put("vnp_TransactionStatus", "00");
        params.put("vnp_SecureHash", "valid");

        when(paymentAdapter.verifyReturn(params)).thenReturn(true);

        Payment payment = Payment.builder()
                .id(2L)
                .bookingId(2L)
                .transactionRef("TXN_VNPAY456")
                .method("VNPAY")
                .amount(BigDecimal.ZERO)
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        when(paymentRepository.findByTransactionRef("TXN_VNPAY456")).thenReturn(Optional.of(payment));

        BookingSeat seat = BookingSeat.builder()
                .seatCode("B1")
                .price(BigDecimal.ZERO)
                .status(BookingSeatStatus.PENDING)
                .build();
        Booking booking = Booking.builder()
                .id(2L)
                .bookingCode("BK_SAGA")
                .userId(1L)
                .showtimeId(1L)
                .totalAmount(BigDecimal.ZERO)
                .status(BookingStatus.PENDING_PAYMENT)
                .holdToken("HOLD_SAGA")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seats(List.of(seat))
                .build();
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(any(BookingEventOutbox.class))).thenReturn(null);

        SeatHold sagaHold = SeatHold.builder()
                .id(2L)
                .holdToken("HOLD_SAGA")
                .userId(1L)
                .showtimeId(1L)
                .status(SeatHoldStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .seats(List.of())
                .build();
        when(seatHoldRepository.findByHoldToken("HOLD_SAGA")).thenReturn(Optional.of(sagaHold));
        when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(null);

        SagaTransaction saga = SagaTransaction.builder()
                .id(1L)
                .bookingId(2L)
                .sagaId("SAGA_TEST")
                .status(SagaStatus.STARTED)
                .build();
        when(sagaTransactionRepository.findByBookingId(2L)).thenReturn(Optional.of(saga));

        BookingResponse response = useCase.handleVnPayReturn(params);

        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());
        verify(sagaTransactionRepository).save(any(SagaTransaction.class));
    }
}
