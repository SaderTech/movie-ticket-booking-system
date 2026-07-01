package com.movieticket.bookingservice.infrastructure.jpa;

import com.movieticket.bookingservice.domain.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings",
       uniqueConstraints = {
           @UniqueConstraint(name = "uc_bookings_code", columnNames = {"booking_code"})
       },
        indexes = {
            @Index(name = "idx_bookings_user_created", columnList = "user_id, created_at"),
            @Index(name = "idx_bookings_showtime_status", columnList = "showtime_id, status")
        }
)
@Getter
@Setter
public class BookingJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_code", nullable = false, length = 50)
    private String bookingCode;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "showtime_id", nullable = false)
    private Long showtimeId;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private BookingStatus status;

    @Column(name = "hold_token", length = 100)
    private String holdToken;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingSeatJpaEntity> seats = new ArrayList<>();

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
