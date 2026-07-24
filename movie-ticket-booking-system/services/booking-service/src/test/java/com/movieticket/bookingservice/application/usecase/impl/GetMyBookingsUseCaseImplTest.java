package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.dto.PagedResponse;
import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.*;
import com.movieticket.bookingservice.domain.repository.BookingRepository;
import com.movieticket.bookingservice.domain.repository.PaymentRepository;
import com.movieticket.bookingservice.domain.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMyBookingsUseCaseImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private PaymentRepository paymentRepository;

    @InjectMocks
    private GetMyBookingsUseCaseImpl useCase;

    @Test
    void findByUserId_NoBookings_ReturnsEmptyPage() {
        when(bookingRepository.findByUserId(eq(1L), any(PageRequest.class))).thenReturn(Page.empty());

        PagedResponse<BookingResponse> response = useCase.findByUserId(1L, 0, 10);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getPage());
        assertEquals(10, response.getSize());
        assertEquals(0, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }

    @Test
    void findByUserId_HasBookings_ReturnsPagedResponse() {
        Booking booking = Booking.builder()
                .id(1L).bookingCode("BK_1").userId(1L).showtimeId(1L)
                .totalAmount(BigDecimal.TEN).status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .seats(List.of())
                .build();
        when(bookingRepository.findByUserId(eq(1L), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(booking)));
        when(ticketRepository.findByBookingId(1L)).thenReturn(List.of());
        when(paymentRepository.findByBookingId(1L)).thenReturn(java.util.Optional.empty());

        PagedResponse<BookingResponse> response = useCase.findByUserId(1L, 0, 10);

        assertEquals(1, response.getItems().size());
        assertEquals("CONFIRMED", response.getItems().get(0).getStatus());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());
    }
}
