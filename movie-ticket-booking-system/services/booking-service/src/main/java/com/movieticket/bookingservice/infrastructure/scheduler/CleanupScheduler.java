package com.movieticket.bookingservice.infrastructure.scheduler;

import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingEventOutboxRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaIdempotencyRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class CleanupScheduler {

    private final JpaIdempotencyRecordRepository idempotencyRecordRepository;
    private final JpaBookingEventOutboxRepository outboxRepository;
    private final RedissonClient redissonClient;

    @Lazy
    @Autowired
    private CleanupScheduler self;

    private static final int OUTBOX_RETENTION_DAYS = 7;
    private static final int DEFAULT_LOCK_WAIT = 2;
    private static final int DEFAULT_LOCK_LEASE = 10;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupExpiredRecords() {
        RLock lock = redissonClient.getLock("scheduler:cleanup");
        if (lock == null) {
            log.warn("Redisson lock for cleanup scheduler is unavailable, skipping run");
            return;
        }
        try {
            if (!lock.tryLock(DEFAULT_LOCK_WAIT, DEFAULT_LOCK_LEASE, TimeUnit.SECONDS)) {
                log.info("Cleanup already running on another instance, skipping");
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock acquisition interrupted for cleanup scheduler");
            return;
        }
        try {
            self.doCleanupExpiredRecords();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                try { lock.unlock(); } catch (Exception e) { log.warn("Error releasing scheduler lock: {}", e.getMessage()); }
            }
        }
    }

    protected void doCleanupExpiredRecords() {
        self.deleteExpiredIdempotencyRecords();
        self.deletePublishedOutboxEvents();
    }

    @Transactional
    protected void deleteExpiredIdempotencyRecords() {
        idempotencyRecordRepository.deleteExpired(LocalDateTime.now());
        log.info("Cleaned up expired idempotency records");
    }

    @Transactional
    protected void deletePublishedOutboxEvents() {
        LocalDateTime outboxCutoff = LocalDateTime.now().minusDays(OUTBOX_RETENTION_DAYS);
        outboxRepository.deletePublishedBefore(outboxCutoff);
        log.info("Cleaned up published outbox events older than {}", outboxCutoff);
    }
}