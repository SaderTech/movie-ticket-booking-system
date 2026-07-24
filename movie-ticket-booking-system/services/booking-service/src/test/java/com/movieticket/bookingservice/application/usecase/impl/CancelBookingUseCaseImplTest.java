package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.application.command.CancelBookingCommand;
import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.*;
import com.movieticket.bookingservice.domain.repository.*;
import com.movieticket.bookingservice.infrastructure.publisher.DomainEventPublisherImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CancelBookingUseCaseImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private SeatHoldRepository seatHoldRepository;
    @Mock private IdempotencyRecordRepository idempotencyRecordRepository;
    @Mock private ObjectMapper objectMapper;
    @Mock private DomainEventPublisherImpl domainEventPublisher;

    @InjectMocks
    private CancelBookingUseCaseImpl useCase;

    @Test
    void execute_BookingNotFound_ThrowsException() {
        when(bookingRepository.findByBookingCode("NOT_FOUND")).thenReturn(Optional.empty());
        CancelBookingCommand cmd = CancelBookingCommand.builder().bookingCode("NOT_FOUND").build();

        assertThrows(ApiException.class, () -> useCase.execute(cmd));
    }

    @Test
    void execute_AlreadyCancelled_ThrowsException() {
        Booking cancelled = Booking.builder()
                .id(1L).bookingCode("BK_CANCELLED").userId(1L)
                .status(BookingStatus.CANCELLED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .seats(List.of())
                .build();
        when(bookingRepository.findByBookingCode("BK_CANCELLED")).thenReturn(Optional.of(cancelled));
        CancelBookingCommand cmd = CancelBookingCommand.builder().bookingCode("BK_CANCELLED").build();

        assertThrows(ApiException.class, () -> useCase.execute(cmd));
    }

    @Test
    void execute_Success_WithTicketsAndPayment() {
        BookingSeat seat = BookingSeat.builder().seatCode("A1").price(BigDecimal.TEN).status(BookingSeatStatus.PENDING).build();
        Booking booking = Booking.builder()
                .id(1L).bookingCode("BK_ACTIVE").userId(1L).showtimeId(1L)
                .totalAmount(BigDecimal.TEN)
                .status(BookingStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .seats(List.of(seat))
                .build();
        Ticket ticket = Ticket.builder().id(1L).bookingId(1L).ticketCode("TCK001").status(TicketStatus.ACTIVE).build();
        Payment payment = Payment.builder().id(1L).bookingId(1L).transactionRef("TXN001").method("VNPAY")
                .amount(BigDecimal.TEN).status(PaymentStatus.PAID).build();

        when(bookingRepository.findByBookingCode("BK_ACTIVE")).thenReturn(Optional.of(booking));
        when(ticketRepository.findByBookingId(1L)).thenReturn(List.of(ticket));
        when(paymentRepository.findByBookingId(1L)).thenReturn(Optional.of(payment));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ticketRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(domainEventPublisher).publishAll(anyList());

        CancelBookingCommand cmd = CancelBookingCommand.builder()
                .bookingCode("BK_ACTIVE").userId(1L).reason("Changed mind").build();
        BookingResponse response = useCase.execute(cmd);

        assertNotNull(response);
        assertEquals("CANCELLED", response.getStatus());
        verify(ticketRepository).saveAll(anyList());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void execute_Success_WithNullReason() {
        BookingSeat seat = BookingSeat.builder().seatCode("A2").price(BigDecimal.ZERO).status(BookingSeatStatus.PENDING).build();
        Booking booking = Booking.builder()
                .id(2L).bookingCode("BK_NOREASON").userId(1L).showtimeId(1L)
                .totalAmount(BigDecimal.ZERO)
                .status(BookingStatus.PENDING_PAYMENT)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .seats(List.of(seat))
                .build();
        when(bookingRepository.findByBookingCode("BK_NOREASON")).thenReturn(Optional.of(booking));
        when(ticketRepository.findByBookingId(2L)).thenReturn(List.of());
        when(paymentRepository.findByBookingId(2L)).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(domainEventPublisher).publishAll(anyList());

        CancelBookingCommand cmd = CancelBookingCommand.builder()
                .bookingCode("BK_NOREASON").userId(1L).build();
        BookingResponse response = useCase.execute(cmd);

        assertNotNull(response);
        assertEquals("CANCELLED", response.getStatus());
        verify(ticketRepository, never()).saveAll(anyList());
        verify(paymentRepository, never()).save(any());
    }
}
