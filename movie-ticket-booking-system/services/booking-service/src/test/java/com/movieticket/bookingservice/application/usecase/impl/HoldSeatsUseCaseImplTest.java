package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.HoldSeatsResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.application.command.HoldSeatsCommand;
import com.movieticket.bookingservice.domain.entity.BookingSetting;
import com.movieticket.bookingservice.domain.port.BookingEventOutboxRepository;
import com.movieticket.bookingservice.domain.port.BookingSettingRepository;
import com.movieticket.bookingservice.domain.port.SeatHoldRepository;
import com.movieticket.bookingservice.domain.port.TicketRepository;
import com.movieticket.bookingservice.infrastructure.client.CinemaClient;
import com.movieticket.bookingservice.infrastructure.client.ShowtimeClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldSeatsUseCaseImplTest {

    @Mock private SeatHoldRepository seatHoldRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private BookingEventOutboxRepository outboxRepository;
    @Mock private BookingSettingRepository bookingSettingRepository;
    @Mock private RedissonClient redissonClient;
    @Mock private ShowtimeClient showtimeClient;
    @Mock private CinemaClient cinemaClient;
    @Mock private RLock lock;

    @InjectMocks
    private HoldSeatsUseCaseImpl useCase;

    private HoldSeatsCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = HoldSeatsCommand.builder()
                .userId(1L)
                .showtimeId(1L)
                .seatCodes(List.of("A1", "A2"))
                .build();
    }

    @Test
    void execute_Success() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(showtimeClient.getShowtime(1L)).thenReturn(Map.of("id", 1));
        when(seatHoldRepository.existsActiveHoldForSeat(anyLong(), anyString(), any())).thenReturn(false);
        when(ticketRepository.existsByShowtimeIdAndSeatCodeAndStatusIn(anyLong(), anyString(), anyList())).thenReturn(false);
        when(bookingSettingRepository.findBySettingKey(anyString())).thenReturn(Optional.empty());
        when(seatHoldRepository.save(any())).thenReturn(null);
        when(outboxRepository.save(any())).thenReturn(null);

        HoldSeatsResponse response = useCase.execute(validCommand);

        assertNotNull(response);
        assertNotNull(response.getHoldToken());
        assertTrue(response.getHoldToken().startsWith("HOLD_"));
        assertNotNull(response.getExpiresAt());
        assertEquals(2, response.getSeats().size());
        verify(redissonClient, times(2)).getLock(anyString());
        verify(lock, times(2)).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
        verify(lock, times(2)).unlock();
    }

    @Test
    void execute_DuplicateSeats_ThrowsException() {
        HoldSeatsCommand command = HoldSeatsCommand.builder()
                .userId(1L)
                .showtimeId(1L)
                .seatCodes(List.of("A1", "A1"))
                .build();

        assertThrows(ApiException.class, () -> useCase.execute(command));
        verify(redissonClient, never()).getLock(anyString());
    }

    @Test
    void execute_ExceedsMaxSeats_ThrowsException() {
        HoldSeatsCommand command = HoldSeatsCommand.builder()
                .userId(1L)
                .showtimeId(1L)
                .seatCodes(List.of("A1", "A2", "A3", "A4", "A5", "A6", "A7", "A8", "A9"))
                .build();

        assertThrows(ApiException.class, () -> useCase.execute(command));
        verify(redissonClient, never()).getLock(anyString());
    }

    @Test
    void execute_LockAcquisitionFailed_ThrowsException() throws InterruptedException {
        when(showtimeClient.getShowtime(1L)).thenReturn(Map.of("id", 1));
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        assertThrows(ApiException.class, () -> useCase.execute(validCommand));
    }

    @Test
    void execute_SeatAlreadyHeld_ThrowsException() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(showtimeClient.getShowtime(1L)).thenReturn(Map.of("id", 1));
        when(seatHoldRepository.existsActiveHoldForSeat(eq(1L), anyString(), any())).thenReturn(true);

        assertThrows(ApiException.class, () -> useCase.execute(validCommand));
        verify(lock, times(2)).unlock();
    }

    @Test
    void execute_SeatAlreadyBooked_ThrowsException() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(showtimeClient.getShowtime(1L)).thenReturn(Map.of("id", 1));
        when(seatHoldRepository.existsActiveHoldForSeat(anyLong(), anyString(), any())).thenReturn(false);
        when(ticketRepository.existsByShowtimeIdAndSeatCodeAndStatusIn(anyLong(), anyString(), anyList())).thenReturn(true);

        assertThrows(ApiException.class, () -> useCase.execute(validCommand));
        verify(lock, times(2)).unlock();
    }
}
