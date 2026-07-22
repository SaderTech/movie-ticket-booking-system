package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

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

    public void issue() {
        if (status != null) {
            throw new IllegalStateException("Only new ticket can be issued, current: " + status);
        }
        status = TicketStatus.ACTIVE;
        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (status == TicketStatus.CANCELLED) {
            return;
        }
        if (status == TicketStatus.USED) {
            throw new IllegalStateException("Cannot cancel a used ticket");
        }
        status = TicketStatus.CANCELLED;
        updatedAt = LocalDateTime.now();
    }

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