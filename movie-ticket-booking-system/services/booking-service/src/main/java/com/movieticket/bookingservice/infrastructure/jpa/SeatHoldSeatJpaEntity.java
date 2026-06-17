package com.movieticket.bookingservice.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_hold_seats",
       uniqueConstraints = {
           @UniqueConstraint(name = "uc_seat_hold_seats_hold_seat", columnNames = {"hold_id", "seat_code"})
       },
        indexes = {
            @Index(name = "idx_seat_hold_seats_showtime_seat", columnList = "showtime_id, seat_code")
        }
)
@Getter
@Setter
public class SeatHoldSeatJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hold_id", nullable = false)
    private SeatHoldJpaEntity seatHold;

    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    @Column(name = "seat_code", nullable = false, length = 20)
    private String seatCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
