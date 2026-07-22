package com.movieticket.bookingservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

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
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hold_id", nullable = false)
    private SeatHold seatHold;

    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    @Column(name = "seat_code", nullable = false, length = 20)
    private String seatCode;

    @Column(name = "seat_type", length = 30)
    private String seatType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}