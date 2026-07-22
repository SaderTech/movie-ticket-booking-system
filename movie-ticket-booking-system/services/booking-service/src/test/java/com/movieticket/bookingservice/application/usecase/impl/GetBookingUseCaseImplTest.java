package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.BookingResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.domain.entity.*;
import com.movieticket.bookingservice.domain.enums.*;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaTicketRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBookingUseCaseImplTest {

    @Mock private JpaBookingRepository bookingRepository;
    @Mock private JpaTicketRepository ticketRepository;

    @InjectMocks
    private GetBookingUseCaseImpl useCase;

    @Test
    void findByBookingCode_NotFound_ThrowsException() {
        when(bookingRepository.findByBookingCode("NOT_FOUND")).thenReturn(Optional.empty());
        assertThrows(ApiException.class, () -> useCase.findByBookingCode("NOT_FOUND", 1L));
    }

    @Test
    void findByBookingCode_Success() {
        Booking booking = Booking.builder()
                .id(1L).bookingCode("BK_OK").userId(1L).showtimeId(1L)
                .totalAmount(BigDecimal.TEN).status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .seats(List.of())
                .build();
        when(bookingRepository.findByBookingCode("BK_OK")).thenReturn(Optional.of(booking));
        when(ticketRepository.findByBookingId(1L)).thenReturn(List.of());

        BookingResponse response = useCase.findByBookingCode("BK_OK", 1L);
        assertNotNull(response);
        assertEquals("CONFIRMED", response.getStatus());
    }

    @Test
    void findByBookingCode_Forbidden_WhenNotOwner() {
        Booking booking = Booking.builder()
                .id(1L).bookingCode("BK_OTHER").userId(2L).showtimeId(1L)
                .totalAmount(BigDecimal.TEN).status(BookingStatus.CONFIRMED)
                .createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now())
                .seats(List.of())
                .build();
        when(bookingRepository.findByBookingCode("BK_OTHER")).thenReturn(Optional.of(booking));

        assertThrows(ApiException.class, () -> useCase.findByBookingCode("BK_OTHER", 1L));
    }
}
