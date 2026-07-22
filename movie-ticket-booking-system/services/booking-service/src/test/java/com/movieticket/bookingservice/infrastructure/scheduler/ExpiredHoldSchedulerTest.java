package com.movieticket.bookingservice.infrastructure.scheduler;

import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingEventOutboxRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaSeatHoldRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RedissonClient;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
}
