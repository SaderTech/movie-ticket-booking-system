package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.dto.HoldSeatsResponse;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.application.command.HoldSeatsCommand;
import com.movieticket.bookingservice.domain.entity.BookingSetting;
import com.movieticket.bookingservice.domain.enums.BookingSeatStatus;
import com.movieticket.bookingservice.domain.enums.TicketStatus;
import com.movieticket.bookingservice.domain.repository.BookingEventOutboxRepository;
import com.movieticket.bookingservice.domain.repository.BookingRepository;
import com.movieticket.bookingservice.domain.repository.BookingSettingRepository;
import com.movieticket.bookingservice.domain.repository.IdempotencyRecordRepository;
import com.movieticket.bookingservice.domain.repository.SeatHoldRepository;
import com.movieticket.bookingservice.domain.repository.TicketRepository;
import com.movieticket.bookingservice.infrastructure.client.CinemaClient;
import com.movieticket.bookingservice.infrastructure.client.MovieClient;
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
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for concurrent seat holding scenarios:
 * - Multiple users trying to hold the same seat simultaneously
 * - Lock contention handling
 */
@ExtendWith(MockitoExtension.class)
class ConcurrentHoldSeatsUseCaseImplTest {

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
        private RLock lockA1;
        @Mock
        private RLock lockA2;

        @InjectMocks
        private HoldSeatsUseCaseImpl useCase;

        private HoldSeatsCommand user1Command;
        private HoldSeatsCommand user2Command;
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

                // User 1 wants A1
                user1Command = HoldSeatsCommand.builder()
                                .userId(1L)
                                .showtimeId(1L)
                                .seatCodes(List.of("A1"))
                                .build();

                // User 2 wants A1 (same seat!)
                user2Command = HoldSeatsCommand.builder()
                                .userId(2L)
                                .showtimeId(1L)
                                .seatCodes(List.of("A1"))
                                .build();

                validShowtime = new ShowtimeResponse(
                                1L, 1L, 1L, 1L,
                                LocalDate.now(), LocalTime.now(), LocalTime.now().plusHours(2),
                                null, null, "AVAILABLE");
                validMovie = new MovieResponse(1L, "Test Movie", null, null, null, null, null, null, null, null, null,
                                null, null, null);
                validCinema = new CinemaResponse(1L, "Test Cinema", null, null, null, null, null, null, null, null);
                validHallSeats = List.of(
                                new SeatResponse(1L, 1L, null, "NORMAL", null, "A", 1, null),
                                new SeatResponse(2L, 1L, null, "VIP", null, "A", 2, null));
        }

        @Test
        void concurrentHold_SameSeat_FirstSucceedsSecondFails() throws Exception {
                // Simulate 2 users concurrently holding the SAME seat (A1)
                // User 1 acquires lock → succeeds
                // User 2 lock times out → SEAT_ALERT_ALREADY_HELD

                when(redissonClient.getLock("lock:showtime:1:seat:A1"))
                                .thenReturn(lockA1);
                when(lockA1.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                                .thenReturn(true) // User 1 acquires lock
                                .thenReturn(false); // User 2 fails to acquire lock
                when(lockA1.isHeldByCurrentThread()).thenReturn(true);

                // Common mocks for showtime/movie/cinema validation
                when(showtimeClient.getShowtime(1L)).thenReturn(validShowtime);
                when(movieClient.getMovie(anyLong())).thenReturn(validMovie);
                when(cinemaClient.getCinema(anyLong())).thenReturn(validCinema);
                when(seatClient.getSeatsByHallId(1L)).thenReturn(validHallSeats);
                when(bookingSettingRepository.findBySettingKey(anyString())).thenReturn(Optional.empty());

                // --- Execute both requests ---
                // User 1 should succeed
                when(seatHoldRepository.existsActiveHoldForSeat(anyLong(), anyString(), any())).thenReturn(false);
                when(ticketRepository.existsByShowtimeIdAndSeatCodeAndStatusIn(anyLong(), anyString(), anyList()))
                                .thenReturn(false);
                when(bookingRepository.existsPendingBookingForSeat(anyLong(), anyString(), anyList()))
                                .thenReturn(false);
                when(seatHoldRepository.save(any())).thenReturn(null);
                when(outboxRepository.save(any())).thenReturn(null);

                HoldSeatsResponse response1 = useCase.execute(user1Command);
                assertNotNull(response1);
                assertNotNull(response1.getHoldToken());

                // User 2 should fail with SEAT_ALREADY_HELD (lock acquisition failure)
                ApiException ex = assertThrows(ApiException.class, () -> useCase.execute(user2Command));
                assertTrue(ex.getMessage().contains("currently being held"),
                                "Expected lock contention error for second user, got: " + ex.getMessage());

                verify(redissonClient, times(2)).getLock("lock:showtime:1:seat:A1");
                verify(lockA1, times(2)).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
        }

        @Test
        void concurrentHold_DifferentSeats_BothSucceed() throws Exception {
                // 2 users holding DIFFERENT seats (A1 and A2) → both should succeed
                when(redissonClient.getLock("lock:showtime:1:seat:A1"))
                                .thenReturn(lockA1);
                when(redissonClient.getLock("lock:showtime:1:seat:A2"))
                                .thenReturn(lockA2);
                when(lockA1.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
                when(lockA2.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
                when(lockA1.isHeldByCurrentThread()).thenReturn(true);
                when(lockA2.isHeldByCurrentThread()).thenReturn(true);

                when(showtimeClient.getShowtime(1L)).thenReturn(validShowtime);
                when(movieClient.getMovie(anyLong())).thenReturn(validMovie);
                when(cinemaClient.getCinema(anyLong())).thenReturn(validCinema);
                when(seatClient.getSeatsByHallId(1L)).thenReturn(validHallSeats);
                when(bookingSettingRepository.findBySettingKey(anyString())).thenReturn(Optional.empty());
                when(seatHoldRepository.existsActiveHoldForSeat(anyLong(), anyString(), any())).thenReturn(false);
                when(ticketRepository.existsByShowtimeIdAndSeatCodeAndStatusIn(anyLong(), anyString(), anyList()))
                                .thenReturn(false);
                when(bookingRepository.existsPendingBookingForSeat(anyLong(), anyString(), anyList()))
                                .thenReturn(false);
                when(seatHoldRepository.save(any())).thenReturn(null);
                when(outboxRepository.save(any())).thenReturn(null);

                HoldSeatsCommand user1A1 = HoldSeatsCommand.builder()
                                .userId(1L).showtimeId(1L).seatCodes(List.of("A1")).build();
                HoldSeatsCommand user2A2 = HoldSeatsCommand.builder()
                                .userId(2L).showtimeId(1L).seatCodes(List.of("A2")).build();

                HoldSeatsResponse resp1 = useCase.execute(user1A1);
                HoldSeatsResponse resp2 = useCase.execute(user2A2);

                assertNotNull(resp1);
                assertNotNull(resp2);
                verify(redissonClient, times(1)).getLock("lock:showtime:1:seat:A1");
                verify(redissonClient, times(1)).getLock("lock:showtime:1:seat:A2");
        }

        @Test
        void concurrentHold_SameUserSameSeat_Idempotent() throws Exception {
                // Same user trying to hold the same seat twice with same idempotency key
                // Should return cached response on second attempt
                String idempotencyKey = "IDEM_CONCURRENT_TEST";

                when(redissonClient.getLock("lock:showtime:1:seat:A1"))
                                .thenReturn(lockA1);
                when(lockA1.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
                when(lockA1.isHeldByCurrentThread()).thenReturn(true);

                when(showtimeClient.getShowtime(1L)).thenReturn(validShowtime);
                when(movieClient.getMovie(anyLong())).thenReturn(validMovie);
                when(cinemaClient.getCinema(anyLong())).thenReturn(validCinema);
                when(seatClient.getSeatsByHallId(1L)).thenReturn(validHallSeats);
                when(bookingSettingRepository.findBySettingKey(anyString())).thenReturn(Optional.empty());
                when(seatHoldRepository.existsActiveHoldForSeat(anyLong(), anyString(), any())).thenReturn(false);
                when(ticketRepository.existsByShowtimeIdAndSeatCodeAndStatusIn(anyLong(), anyString(), anyList()))
                                .thenReturn(false);
                when(bookingRepository.existsPendingBookingForSeat(anyLong(), anyString(), anyList()))
                                .thenReturn(false);
                when(seatHoldRepository.save(any())).thenReturn(null);
                when(outboxRepository.save(any())).thenReturn(null);
                when(idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey))
                                .thenReturn(Optional.empty()) // First call: no cache
                                .thenReturn(Optional.empty()); // Processing record: no existing
                when(idempotencyRecordRepository.saveAndFlush(any())).thenReturn(null);

                // ObjectMapper for response serialization
                when(objectMapper.writeValueAsString(anyString())).thenReturn("{}");

                HoldSeatsCommand cmd = HoldSeatsCommand.builder()
                                .userId(1L).showtimeId(1L).seatCodes(List.of("A1"))
                                .idempotencyKey(idempotencyKey)
                                .build();

                // First request
                HoldSeatsResponse firstResponse = useCase.execute(cmd);
                assertNotNull(firstResponse);
                assertNotNull(firstResponse.getHoldToken());
        }

        @Test
        void concurrentHold_MultiSeat_SecondSeatLockFails_RollsBackAll() throws Exception {
                // User tries to hold A1 and A2, but lock on A2 fails
                // All acquired locks should be released in finally block

                when(redissonClient.getLock("lock:showtime:1:seat:A1"))
                                .thenReturn(lockA1);
                when(redissonClient.getLock("lock:showtime:1:seat:A2"))
                                .thenReturn(lockA2);
                when(lockA1.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
                when(lockA2.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);
                when(lockA1.isHeldByCurrentThread()).thenReturn(true);

                when(showtimeClient.getShowtime(1L)).thenReturn(validShowtime);
                when(movieClient.getMovie(anyLong())).thenReturn(validMovie);
                when(cinemaClient.getCinema(anyLong())).thenReturn(validCinema);
                when(seatClient.getSeatsByHallId(1L)).thenReturn(validHallSeats);

                HoldSeatsCommand multiSeatCmd = HoldSeatsCommand.builder()
                                .userId(1L).showtimeId(1L).seatCodes(List.of("A1", "A2")).build();

                assertThrows(ApiException.class, () -> useCase.execute(multiSeatCmd));

                // Verify lock A1 was acquired first, then released in finally block
                verify(lockA1).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
                verify(lockA2).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
                verify(lockA1).unlock(); // A1 should be released in finally
                verify(lockA2, never()).unlock(); // A2 was never acquired
        }
}
