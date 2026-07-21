package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.*;
import com.movieticket.bookingservice.domain.port.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMyBookingsUseCaseImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private TicketRepository ticketRepository;

    @InjectMocks
    private GetMyBookingsUseCaseImpl useCase;

    @Test
    void findByUserId_NoBookings_ReturnsEmptyList() {
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of());
        List<BookingResponse> responses = useCase.findByUserId(1L);
        assertTrue(responses.isEmpty());
    }

    @Test
    void findByUserId_HasBookings_ReturnsList() {
        Booking booking = Booking.builder()
                .id(1L).bookingCode("BK_1").userId(1L).showtimeId(1L)
                .totalAmount(BigDecimal.TEN).status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .seats(List.of())
                .build();
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(booking));
        when(ticketRepository.findByBookingId(1L)).thenReturn(List.of());

        List<BookingResponse> responses = useCase.findByUserId(1L);
        assertEquals(1, responses.size());
        assertEquals("CONFIRMED", responses.get(0).getStatus());
    }
}
