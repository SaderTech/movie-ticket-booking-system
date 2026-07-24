package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.HoldSeatsResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.application.command.HoldSeatsCommand;
import com.movieticket.bookingservice.domain.entity.BookingSetting;
import com.movieticket.bookingservice.domain.repository.BookingEventOutboxRepository;
import com.movieticket.bookingservice.domain.repository.BookingRepository;
import com.movieticket.bookingservice.domain.repository.BookingSettingRepository;
import com.movieticket.bookingservice.domain.repository.IdempotencyRecordRepository;
import com.movieticket.bookingservice.domain.repository.SeatHoldRepository;
import com.movieticket.bookingservice.domain.repository.TicketRepository;
import com.movieticket.bookingservice.infrastructure.client.MovieClient;
import com.movieticket.bookingservice.infrastructure.client.CinemaClient;
import com.movieticket.bookingservice.infrastructure.client.SeatClient;
import com.movieticket.bookingservice.infrastructure.client.ShowtimeClient;
import com.movieticket.bookingservice.infrastructure.client.dto.CinemaResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.MovieResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.SeatResponse;
import com.movieticket.bookingservice.infrastructure.client.dto.ShowtimeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoldSeatsUseCaseImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private SeatHoldRepository seatHoldRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private BookingEventOutboxRepository outboxRepository;
    @Mock
    private BookingSettingRepository bookingSettingRepository;
    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private ShowtimeClient showtimeClient;
    @Mock
    private MovieClient movieClient;
    @Mock
    private CinemaClient cinemaClient;
    @Mock
    private SeatClient seatClient;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private RLock lock;

    @InjectMocks
    private HoldSeatsUseCaseImpl useCase;

    private HoldSeatsCommand validCommand;
    private ShowtimeResponse validShowtime;
    private List<SeatResponse> validHallSeats;
    private MovieResponse validMovie;
    private CinemaResponse validCinema;

    @BeforeEach
    void setUp() {
        try {
            Field selfField = HoldSeatsUseCaseImpl.class.getDeclaredField("self");
            selfField.setAccessible(true);
            selfField.set(useCase, useCase);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set self reference", e);
        }

        validCommand = HoldSeatsCommand.builder()
                .userId(1L)
                .showtimeId(1L)
                .seatCodes(List.of("A1", "A2"))
                .build();
        validShowtime = new ShowtimeResponse(
                1L, 1L, 1L, 1L,
                LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(2),
                null, null, "AVAILABLE");
        validMovie = new MovieResponse(1L, "Test Movie", null, null, null, null, null, null, null, null, null, null,
                null, null);
        validCinema = new CinemaResponse(1L, "Test Cinema", null, null, null, null, null, null, null, null);
        validHallSeats = List.of(
                new SeatResponse(1L, 1L, null, "NORMAL", null, "A", 1, null),
                new SeatResponse(2L, 1L, null, "VIP", null, "A", 2, null));
    }

    @Test
    void execute_Success() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(showtimeClient.getShowtime(1L)).thenReturn(validShowtime);
        when(movieClient.getMovie(1L)).thenReturn(validMovie);
        when(cinemaClient.getCinema(1L)).thenReturn(validCinema);
        when(seatClient.getSeatsByHallId(1L)).thenReturn(validHallSeats);
        when(seatHoldRepository.existsActiveHoldForSeat(anyLong(), anyString(), any())).thenReturn(false);
        when(ticketRepository.existsByShowtimeIdAndSeatCodeAndStatusIn(anyLong(), anyString(), anyList()))
                .thenReturn(false);
        when(bookingRepository.existsPendingBookingForSeat(anyLong(), anyString(), anyList())).thenReturn(false);
        when(bookingSettingRepository.findBySettingKey(anyString())).thenReturn(Optional.empty());
        when(seatHoldRepository.save(any())).thenReturn(null);
        when(outboxRepository.save(any())).thenReturn(null);

        HoldSeatsResponse response = useCase.execute(validCommand);

        assertNotNull(response);
        assertNotNull(response.getHoldToken());
        assertTrue(response.getHoldToken().startsWith("HOLD_"));
        assertNotNull(response.getExpiresAt());
        assertEquals(2, response.getSeats().size());
        assertEquals("NORMAL", response.getSeats().get(0).getSeatType());
        assertEquals("VIP", response.getSeats().get(1).getSeatType());
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
        when(showtimeClient.getShowtime(1L)).thenReturn(validShowtime);
        when(movieClient.getMovie(1L)).thenReturn(validMovie);
        when(cinemaClient.getCinema(1L)).thenReturn(validCinema);
        when(seatClient.getSeatsByHallId(1L)).thenReturn(validHallSeats);
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        assertThrows(ApiException.class, () -> useCase.execute(validCommand));
    }

    @Test
    void execute_SeatAlreadyHeld_ThrowsException() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(showtimeClient.getShowtime(1L)).thenReturn(validShowtime);
        when(movieClient.getMovie(1L)).thenReturn(validMovie);
        when(cinemaClient.getCinema(1L)).thenReturn(validCinema);
        when(seatClient.getSeatsByHallId(1L)).thenReturn(validHallSeats);
        when(seatHoldRepository.existsActiveHoldForSeat(eq(1L), anyString(), any())).thenReturn(true);

        assertThrows(ApiException.class, () -> useCase.execute(validCommand));
        verify(lock, times(2)).unlock();
    }

    @Test
    void execute_SeatAlreadyBooked_ThrowsException() throws InterruptedException {
        when(redissonClient.getLock(anyString())).thenReturn(lock);
        when(lock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);
        when(showtimeClient.getShowtime(1L)).thenReturn(validShowtime);
        when(movieClient.getMovie(1L)).thenReturn(validMovie);
        when(cinemaClient.getCinema(1L)).thenReturn(validCinema);
        when(seatClient.getSeatsByHallId(1L)).thenReturn(validHallSeats);
        when(seatHoldRepository.existsActiveHoldForSeat(anyLong(), anyString(), any())).thenReturn(false);
        when(ticketRepository.existsByShowtimeIdAndSeatCodeAndStatusIn(anyLong(), anyString(), anyList()))
                .thenReturn(true);

        assertThrows(ApiException.class, () -> useCase.execute(validCommand));
        verify(lock, times(2)).unlock();
    }
}
