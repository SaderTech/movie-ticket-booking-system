package com.movieticket.bookingservice.infrastructure.scheduler;

import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.entity.SeatHold;
import com.movieticket.bookingservice.domain.entity.SeatHoldSeat;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingEventOutboxRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaSeatHoldRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExpiredHoldSchedulerTest {

    @Mock
    private JpaSeatHoldRepository seatHoldRepository;

    @Mock
    private JpaBookingEventOutboxRepository outboxRepository;

    @Mock
    private RedissonClient redissonClient;

    @InjectMocks
    private ExpiredHoldScheduler scheduler;

    @Test
    void expireStaleHolds_WhenLockFactoryReturnsNull_DoesNotThrow() {
        when(redissonClient.getLock("scheduler:expired-hold")).thenReturn(null);

        assertDoesNotThrow(() -> scheduler.expireStaleHolds());

        verifyNoInteractions(seatHoldRepository);
        verifyNoInteractions(outboxRepository);
    }

    @Test
    void processExpiredHold_RefetchesHoldAndCreatesOutboxEvent() {
        SeatHold hold = SeatHold.builder()
                .id(42L)
                .holdToken("HOLD_EXPIRED")
                .userId(7L)
                .showtimeId(11L)
                .status(SeatHoldStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .seats(new ArrayList<>())
                .build();
        hold.getSeats().add(SeatHoldSeat.builder()
                .seatHold(hold)
                .showtimeId(11L)
                .seatCode("A1")
                .seatType("NORMAL")
                .build());
        when(seatHoldRepository.findById(42L)).thenReturn(Optional.of(hold));

        scheduler.processExpiredHold(42L);

        assertEquals(SeatHoldStatus.EXPIRED, hold.getStatus());
        verify(seatHoldRepository).save(hold);

        ArgumentCaptor<BookingEventOutbox> eventCaptor =
                ArgumentCaptor.forClass(BookingEventOutbox.class);
        verify(outboxRepository).save(eventCaptor.capture());
        BookingEventOutbox event = eventCaptor.getValue();
        assertEquals(OutboxStatus.PENDING, event.getStatus());
        assertEquals("SEAT_HOLD_EXPIRED", event.getEventType());
        assertTrue(event.getPayloadJson().contains("\"seatCodes\":[\"A1\"]"));
    }
}
