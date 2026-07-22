package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.BookingSeatStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking_seats",
       indexes = {
           @Index(name = "idx_booking_seats_showtime_seat", columnList = "showtime_id, seat_code"),
           @Index(name = "idx_booking_seats_booking_id", columnList = "booking_id")
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    @Column(name = "seat_code", nullable = false, length = 20)
    private String seatCode;

    @Column(name = "seat_type", length = 30)
    private String seatType;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BookingSeatStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void assignToBooking(Long bookingId) {
        // This method is kept for backward compatibility but shouldn't be used with JPA
        // The booking relationship is managed by the @ManyToOne mapping
    }

    public void confirm() {
        if (status != BookingSeatStatus.PENDING) {
            throw new IllegalStateException("Only pending seat can be confirmed, current: " + status);
        }
        status = BookingSeatStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == BookingSeatStatus.CANCELLED) {
            return;
        }
        status = BookingSeatStatus.CANCELLED;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}