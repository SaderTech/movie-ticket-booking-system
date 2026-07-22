package com.movieticket.bookingservice.domain.entity;

import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seat_holds",
       uniqueConstraints = {
           @UniqueConstraint(name = "uc_seat_holds_token", columnNames = {"hold_token"})
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hold_token", nullable = false, length = 100)
    private String holdToken;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private SeatHoldStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "seatHold", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SeatHoldSeat> seats = new ArrayList<>();

    public boolean isActive() {
        return status == SeatHoldStatus.ACTIVE && expiresAt != null && expiresAt.isAfter(LocalDateTime.now());
    }

    public void convert() {
        if (status != SeatHoldStatus.ACTIVE) {
            throw new IllegalStateException("Only active hold can be converted, current: " + status);
        }
        status = SeatHoldStatus.CONVERTED;
        updatedAt = LocalDateTime.now();
    }

    public void expire() {
        if (status != SeatHoldStatus.ACTIVE) {
            return;
        }
        status = SeatHoldStatus.EXPIRED;
        updatedAt = LocalDateTime.now();
    }

    public void release() {
        if (status != SeatHoldStatus.ACTIVE) {
            return;
        }
        status = SeatHoldStatus.RELEASED;
        updatedAt = LocalDateTime.now();
    }

    public void extendExpiry(int additionalMinutes) {
        if (status != SeatHoldStatus.ACTIVE) {
            throw new IllegalStateException("Cannot extend expiry for hold with status: " + status);
        }
        this.expiresAt = this.expiresAt.plusMinutes(additionalMinutes);
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}