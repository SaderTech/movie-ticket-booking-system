package com.movieticket.bookingservice.infrastructure.jpa;

import com.movieticket.bookingservice.domain.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "tickets",
       uniqueConstraints = {
           @UniqueConstraint(name = "uc_tickets_code", columnNames = {"ticket_code"}),
           @UniqueConstraint(name = "uc_tickets_showtime_seat", columnNames = {"showtime_id", "seat_code"})
       },
       indexes = {
            @Index(name = "idx_tickets_booking_id", columnList = "booking_id")
       }
)
@Getter
@Setter
public class TicketJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_code", nullable = false, length = 50)
    private String ticketCode;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "movie_title", length = 255)
    private String movieTitle;

    @Column(name = "movie_poster_url", length = 500)
    private String moviePosterUrl;

    @Column(name = "cinema_id")
    private Long cinemaId;

    @Column(name = "cinema_name", length = 255)
    private String cinemaName;

    @Column(name = "hall_id")
    private Long hallId;

    @Column(name = "hall_name", length = 255)
    private String hallName;

    @Column(name = "seat_code", nullable = false, length = 20)
    private String seatCode;

    @Column(name = "seat_type", length = 30)
    private String seatType;

    @Column(name = "show_date")
    private LocalDate showDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "qr_payload", columnDefinition = "TEXT")
    private String qrPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TicketStatus status;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
