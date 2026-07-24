package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.application.command.ConfirmBookingCommand;
import com.movieticket.bookingservice.domain.aggregate.BookingAggregate;
import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.*;
import com.movieticket.bookingservice.domain.repository.*;
import com.movieticket.bookingservice.infrastructure.publisher.DomainEventPublisherImpl;
import com.movieticket.bookingservice.infrastructure.adapter.PaymentAdapter;
import com.movieticket.bookingservice.infrastructure.client.CinemaClient;
import com.movieticket.bookingservice.infrastructure.client.MovieClient;
import com.movieticket.bookingservice.infrastructure.client.SeatClient;
import com.movieticket.bookingservice.infrastructure.client.ShowtimeClient;
import com.movieticket.bookingservice.infrastructure.client.dto.CinemaResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.MovieResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.ShowtimeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfirmBookingUseCaseImplTest {

    @Mock
    private SeatHoldRepository seatHoldRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private SagaTransactionRepository sagaTransactionRepository;
    @Mock
    private PaymentAdapter paymentAdapter;
    @Mock
    private DomainEventPublisherImpl domainEventPublisher;
    @Mock
    private BookingSettingRepository bookingSettingRepository;
    @Mock
    private ShowtimeClient showtimeClient;
    @Mock
    private MovieClient movieClient;
    @Mock
    private CinemaClient cinemaClient;
    @Mock
    private SeatClient seatClient;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock confirmLock;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;

        @InjectMocks
        private ConfirmBookingUseCaseImpl useCase;

        private SeatHold activeHold;
        private ConfirmBookingCommand mockCommand;
        private ConfirmBookingCommand vnpayCommand;

        @BeforeEach
        void setUp() {
                lenient().when(redissonClient.getLock(anyString())).thenReturn(confirmLock);
                try {
                        lenient().when(confirmLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
                } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                }
                lenient().when(confirmLock.isHeldByCurrentThread()).thenReturn(true);
                lenient().when(showtimeClient.getShowtime(1L)).thenReturn(new ShowtimeResponse(
                                1L, 1L, 1L, 1L, java.time.LocalDate.now(), java.time.LocalTime.now(),
                                java.time.LocalTime.now().plusHours(2), BigDecimal.valueOf(120000), null, null));
                try {
                        Field selfField = ConfirmBookingUseCaseImpl.class.getDeclaredField("self");
                        selfField.setAccessible(true);
                        selfField.set(useCase, useCase);
                } catch (Exception e) {
                        throw new RuntimeException("Failed to set self reference", e);
                }

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
                                .paymentMethod("VNPAY")
                                .ipAddress("127.0.0.1")
                                .build();

                vnpayCommand = ConfirmBookingCommand.builder()
                                .userId(1L)
                                .holdToken("HOLD_TEST123")
                                .paymentMethod("VNPAY")
                                .ipAddress("127.0.0.1")
                                .build();
        }

        @Test
        void execute_UsesConfiguredLockWaitAndLease() throws InterruptedException {
                when(bookingSettingRepository.findBySettingKey("lock_wait_time_seconds"))
                                .thenReturn(Optional.of(BookingSetting.builder().settingKey("lock_wait_time_seconds")
                                                .settingValue("2").build()));
                when(bookingSettingRepository.findBySettingKey("lock_lease_time_seconds"))
                                .thenReturn(Optional.of(BookingSetting.builder().settingKey("lock_lease_time_seconds")
                                                .settingValue("10").build()));
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
                when(sagaTransactionRepository.save(any(SagaTransaction.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(paymentAdapter.createPaymentUrl(any(Booking.class), any(Payment.class), eq("127.0.0.1"), any()))
                                .thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=TEST&vnp_SecureHash=abc");

                useCase.execute(vnpayCommand);

                verify(confirmLock).tryLock(2L, 10L, TimeUnit.SECONDS);
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
                                .userId(1L).holdToken("HOLD_EXPIRED").paymentMethod("VNPAY").build();

                assertThrows(ApiException.class, () -> useCase.execute(cmd));
        }

        // ==================== VALIDATION ====================

        @Test
        void execute_NonVnPayMethod_ThrowsException() {
                ConfirmBookingCommand nonVnpay = ConfirmBookingCommand.builder()
                                .userId(1L).holdToken("HOLD_TEST123").paymentMethod("MOCK").build();

                assertThrows(ApiException.class, () -> useCase.execute(nonVnpay));
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
                when(sagaTransactionRepository.save(any(SagaTransaction.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(paymentAdapter.createPaymentUrl(any(Booking.class), any(Payment.class), eq("127.0.0.1"), any()))
                                .thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=TEST&vnp_SecureHash=abc");

                BookingResponse response = useCase.execute(vnpayCommand);

                assertNotNull(response);
                assertEquals("PENDING_PAYMENT", response.getStatus());
                assertNotNull(response.getPaymentUrl());
                assertTrue(response.getPaymentUrl().startsWith("https://sandbox.vnpayment.vn"));
                assertTrue(response.getTickets().isEmpty());
                verify(domainEventPublisher, never()).publishAll(anyList());
        }

        @Test
        void execute_VnPayFlow_Phase1_ExtendsHoldExpiry() {
                // Arrange
                LocalDateTime originalExpiry = LocalDateTime.now().plusMinutes(5);
                activeHold = SeatHold.builder()
                                .id(1L)
                                .holdToken("HOLD_TEST123")
                                .userId(1L)
                                .showtimeId(1L)
                                .status(SeatHoldStatus.ACTIVE)
                                .expiresAt(originalExpiry)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .seats(List.of(SeatHoldSeat.builder().showtimeId(1L).seatCode("A1").build()))
                                .build();

                when(bookingSettingRepository.findBySettingKey("hold_payment_extension_minutes"))
                                .thenReturn(Optional.of(BookingSetting.builder()
                                                .settingKey("hold_payment_extension_minutes")
                                                .settingValue("30")
                                                .build()));
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
                when(sagaTransactionRepository.save(any(SagaTransaction.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(paymentAdapter.createPaymentUrl(any(Booking.class), any(Payment.class), eq("127.0.0.1"), any()))
                                .thenReturn("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_TxnRef=TEST&vnp_SecureHash=abc");

                // Act
                useCase.execute(vnpayCommand);

                // Assert: Hold expiry should be extended by 30 minutes
                assertTrue(activeHold.getExpiresAt().isAfter(originalExpiry),
                                "Hold expiry should be extended after VNPay redirect");
                assertEquals(originalExpiry.plusMinutes(30), activeHold.getExpiresAt(),
                                "Hold expiry should be extended by exactly 30 minutes");
                verify(seatHoldRepository, times(1)).save(activeHold);
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
                when(seatHoldRepository.findByHoldToken("HOLD_TEST123")).thenReturn(Optional.of(activeHold));
                when(seatHoldRepository.save(any(SeatHold.class))).thenReturn(null);
                SagaTransaction saga = SagaTransaction.builder()
                                .id(1L)
                                .bookingId(1L)
                                .sagaId("SAGA_VNPAY123")
                                .status(SagaStatus.STARTED)
                                .currentStep("PAYMENT")
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build();
                when(sagaTransactionRepository.findByBookingId(1L)).thenReturn(Optional.of(saga));
                when(sagaTransactionRepository.save(any(SagaTransaction.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));
                lenient().when(showtimeClient.getShowtime(1L)).thenReturn(new ShowtimeResponse(
                                1L, 1L, 1L, 1L,
                                java.time.LocalDate.now(), java.time.LocalTime.now(),
                                java.time.LocalTime.now().plusHours(2),
                                null, null, null));
                lenient().when(movieClient.getMovie(1L)).thenReturn(new MovieResponse(
                                1L, "Test Movie", null, null, null, "", null, null, null, null, null, null, null,
                                null));
                lenient().when(cinemaClient.getCinema(1L)).thenReturn(new CinemaResponse(
                                1L, "Test Cinema", null, null, null, null, null, null, null, null));
                lenient().doNothing().when(domainEventPublisher).publishAll(anyList());

                BookingResponse response = useCase.handleVnPayReturn(params);

                assertNotNull(response);
                assertEquals("CONFIRMED", response.getStatus());
                assertEquals(PaymentStatus.PAID, payment.getStatus());
                verify(ticketRepository, times(1)).saveAll(anyList());
                @SuppressWarnings("unchecked")
                ArgumentCaptor<List<Ticket>> ticketsCaptor = ArgumentCaptor.forClass(List.class);
                verify(ticketRepository).saveAll(ticketsCaptor.capture());
                assertEquals("Phòng chiếu 1", ticketsCaptor.getValue().get(0).getHallName());
        }

        @Test
        void handleVnPayReturn_LateSuccessfulPaymentMarksRefundPendingAndReleasesBooking() {
                Map<String, String> params = new HashMap<>();
                params.put("vnp_TxnRef", "TXN_LATE_PAYMENT");
                params.put("vnp_ResponseCode", "00");
                params.put("vnp_TransactionStatus", "00");
                params.put("vnp_SecureHash", "valid");
                when(paymentAdapter.verifyReturn(params)).thenReturn(true);

                Payment payment = Payment.builder()
                                .id(1L).bookingId(1L).transactionRef("TXN_LATE_PAYMENT")
                                .method("VNPAY").amount(BigDecimal.valueOf(180000))
                                .status(PaymentStatus.PENDING).build();
                BookingSeat seat = BookingSeat.builder().seatCode("A1").price(BigDecimal.valueOf(90000))
                                .status(BookingSeatStatus.PENDING).build();
                Booking booking = Booking.builder()
                                .id(1L).bookingCode("BK_LATE").userId(1L).showtimeId(1L)
                                .totalAmount(BigDecimal.valueOf(180000)).status(BookingStatus.PENDING_PAYMENT)
                                .holdToken("HOLD_LATE").seats(List.of(seat)).build();
                SeatHold expiredHold = SeatHold.builder()
                                .id(1L).holdToken("HOLD_LATE").userId(1L).showtimeId(1L)
                                .status(SeatHoldStatus.EXPIRED).expiresAt(LocalDateTime.now().minusMinutes(1))
                                .seats(List.of()).build();
                SagaTransaction saga = SagaTransaction.builder()
                                .id(1L).bookingId(1L).sagaId("SAGA_LATE").status(SagaStatus.STARTED).build();

                when(paymentRepository.findByTransactionRef("TXN_LATE_PAYMENT")).thenReturn(Optional.of(payment));
                when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
                when(seatHoldRepository.findByHoldToken("HOLD_LATE")).thenReturn(Optional.of(expiredHold));
                when(sagaTransactionRepository.findByBookingId(1L)).thenReturn(Optional.of(saga));
                when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(seatHoldRepository.save(any(SeatHold.class))).thenAnswer(invocation -> invocation.getArgument(0));
                when(sagaTransactionRepository.save(any(SagaTransaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

                BookingResponse response = useCase.handleVnPayReturn(params);

                assertEquals("FAILED", response.getStatus());
                assertEquals(PaymentStatus.REFUND_PENDING, payment.getStatus());
                assertEquals(BookingSeatStatus.CANCELLED, seat.getStatus());
                assertEquals(SagaStatus.COMPENSATED, saga.getStatus());
                verify(domainEventPublisher).publishAll(argThat(events -> events.size() == 2));
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

                BookingResponse response = useCase.handleVnPayReturn(params);
                assertEquals("FAILED", response.getStatus());
                assertEquals(PaymentStatus.FAILED, payment.getStatus());
                assertEquals(BookingStatus.FAILED, booking.getStatus());
                assertEquals(BookingSeatStatus.CANCELLED, seat.getStatus());
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
                lenient().doNothing().when(domainEventPublisher).publishAll(anyList());

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
