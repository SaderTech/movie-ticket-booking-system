package com.movieticket.bookingservice.infrastructure.scheduler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieticket.bookingservice.domain.entity.BookingEventOutbox;
import com.movieticket.bookingservice.domain.enums.OutboxStatus;
import com.movieticket.bookingservice.domain.port.BookingEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublishScheduler {

    private final BookingEventOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.enabled:false}")
    private boolean kafkaEnabled;

    private static final int MAX_RETRIES = 5;
    private static final int BATCH_SIZE = 50;
    private static final int SEND_TIMEOUT_SECONDS = 10;

    @Scheduled(fixedRate = 15000)
    @Transactional
    public void publishPendingEvents() {
        if (!kafkaEnabled) {
            return;
        }

        List<BookingEventOutbox> pending = outboxRepository.findPendingEvents(OutboxStatus.PENDING, BATCH_SIZE);
        if (pending.isEmpty()) {
            return;
        }

        log.info("Publishing {} pending outbox events", pending.size());

        for (BookingEventOutbox outbox : pending) {
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
}
