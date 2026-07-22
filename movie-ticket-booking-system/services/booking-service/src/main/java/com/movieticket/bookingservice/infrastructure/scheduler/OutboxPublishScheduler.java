package com.movieticket.bookingservice.infrastructure.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingEventOutboxRepository;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OutboxPublishScheduler {

    private final JpaBookingEventOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedissonClient redissonClient;

    public OutboxPublishScheduler(
            JpaBookingEventOutboxRepository outboxRepository,
            ObjectMapper objectMapper,
            RedissonClient redissonClient,
            @Qualifier("jsonKafkaTemplate") KafkaTemplate<String, Object> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.objectMapper = objectMapper;
        this.redissonClient = redissonClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Lazy
    @Autowired
    private OutboxPublishScheduler self;

    @Value("${app.kafka.enabled:false}")
    private boolean kafkaEnabled;

    private static final int MAX_RETRIES = 5;
    private static final int BATCH_SIZE = 50;
    private static final int SEND_TIMEOUT_SECONDS = 10;
    private static final int DEFAULT_LOCK_WAIT = 2;
    private static final int DEFAULT_LOCK_LEASE = 10;

    @Scheduled(fixedRate = 15000)
    public void publishPendingEvents() {
        if (!kafkaEnabled) {
            return;
        }

        RLock lock = redissonClient.getLock("scheduler:outbox-publish");
        if (lock == null) {
            log.warn("Redisson lock for outbox-publish scheduler is unavailable, skipping run");
            return;
        }
        try {
            if (!lock.tryLock(DEFAULT_LOCK_WAIT, DEFAULT_LOCK_LEASE, TimeUnit.SECONDS)) {
                log.info("Outbox publish already running on another instance, skipping");
                return;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Lock acquisition interrupted for outbox publish scheduler");
            return;
        }
        try {
            self.doPublishPendingEvents();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                try { lock.unlock(); } catch (Exception e) { log.warn("Error releasing scheduler lock: {}", e.getMessage()); }
            }
        }
    }

    protected void doPublishPendingEvents() {
        List<BookingEventOutbox> pending = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING, PageRequest.of(0, BATCH_SIZE));
        if (pending.isEmpty()) {
            return;
        }

        log.info("Publishing {} pending outbox events", pending.size());

        for (BookingEventOutbox outbox : pending) {
            self.processOutboxEvent(outbox);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processOutboxEvent(BookingEventOutbox outbox) {
        try {
            JsonNode payload = objectMapper.readTree(outbox.getPayloadJson());
            kafkaTemplate.send(outbox.getTopic(), outbox.getAggregateId(), payload)
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            outbox.markPublished();
            outboxRepository.save(outbox);
            log.debug("Published outbox event: {} [{}] -> {}", outbox.getEventType(), outbox.getEventId(), outbox.getTopic());
        } catch (Exception e) {
            outbox.incrementRetry();
            String error = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();

            if (outbox.getRetryCount() >= MAX_RETRIES) {
                outbox.markFailed(error);
                log.error("Outbox event {} failed after {} retries: {}", outbox.getEventId(), MAX_RETRIES, error);
            } else {
                log.warn("Failed to publish outbox event {} (retry {}/{}): {}",
                        outbox.getEventId(), outbox.getRetryCount(), MAX_RETRIES, error);
            }
            outboxRepository.save(outbox);
        }
    }
}