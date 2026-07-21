package com.movieticket.cinemaservice.domain.aggregate.seattype;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "seat_types")
@Getter
@Setter
@NoArgsConstructor
public class SeatType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public SeatType(String code, String name, String description) {
        validateCode(code);
        validateName(name);
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.description = description;
    }

    public void update(String code, String name, String description) {
        validateCode(code);
        validateName(name);
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.description = description;
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Seat type code must not be blank");
        }
        if (code.trim().length() > 50) {
            throw new IllegalArgumentException("Seat type code must not exceed 50 characters");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Seat type name must not be blank");
        }
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Seat type name must not exceed 100 characters");
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
