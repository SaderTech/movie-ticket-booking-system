package com.movieticket.cinemaservice.domain.aggregate.hall;

import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.domain.enums.SeatStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "seats",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_seat_hall_row_number", columnNames = {"hall_id", "row_name", "seat_number"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_type_id", nullable = false)
    private SeatType seatType;

    @Column(name = "row_name", nullable = false, length = 10)
    private String rowName;

    @Column(name = "seat_number", nullable = false)
    private Integer seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SeatStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Seat(Hall hall, SeatType seatType, String rowName, Integer seatNumber, SeatStatus status) {
        validateHall(hall);
        validateSeatType(seatType);
        validateRowName(rowName);
        validateSeatNumber(seatNumber);

        this.hall = hall;
        this.seatType = seatType;
        this.rowName = rowName.trim().toUpperCase();
        this.seatNumber = seatNumber;
        this.status = status == null ? SeatStatus.ACTIVE : status;
    }

    public void update(SeatType seatType, String rowName, Integer seatNumber, SeatStatus status) {
        validateSeatType(seatType);
        validateRowName(rowName);
        validateSeatNumber(seatNumber);
        if (status == null) {
            throw new IllegalArgumentException("Seat status must not be null when updating");
        }

        this.seatType = seatType;
        this.rowName = rowName.trim().toUpperCase();
        this.seatNumber = seatNumber;
        this.status = status;
    }

    private void validateHall(Hall hall) {
        if (hall == null) {
            throw new IllegalArgumentException("Hall must not be null");
        }
    }

    private void validateSeatType(SeatType seatType) {
        if (seatType == null) {
            throw new IllegalArgumentException("Seat type must not be null");
        }
    }

    private void validateRowName(String rowName) {
        if (rowName == null || rowName.isBlank()) {
            throw new IllegalArgumentException("Row name must not be blank");
        }
        if (rowName.trim().length() > 10) {
            throw new IllegalArgumentException("Row name must not exceed 10 characters");
        }
    }

    private void validateSeatNumber(Integer seatNumber) {
        if (seatNumber == null || seatNumber <= 0) {
            throw new IllegalArgumentException("Seat number must be greater than 0");
        }
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
