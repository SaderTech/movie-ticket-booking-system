package com.movieticket.movieservice.domain.aggregate.director;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "directors")
@Getter
@NoArgsConstructor
public class Director {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Director(String name, String biography, LocalDate birthDate) {
        validate(name, biography, birthDate);
        this.name = normalizeRequired(name);
        this.biography = normalizeNullable(biography);
        this.birthDate = birthDate;
    }

    public void update(String name, String biography, LocalDate birthDate) {
        validate(name, biography, birthDate);
        this.name = normalizeRequired(name);
        this.biography = normalizeNullable(biography);
        this.birthDate = birthDate;
    }

    private void validate(String name, String biography, LocalDate birthDate) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Director name must not be blank");
        }

        if (name.trim().length() > 255) {
            throw new IllegalArgumentException("Director name must not exceed 255 characters");
        }

        if (biography != null && biography.length() > 5000) {
            throw new IllegalArgumentException("Director biography must not exceed 5000 characters");
        }

        if (birthDate != null && !birthDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Director birth date must be in the past");
        }
    }

    private String normalizeRequired(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
