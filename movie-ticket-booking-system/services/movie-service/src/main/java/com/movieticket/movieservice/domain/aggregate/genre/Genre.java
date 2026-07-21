package com.movieticket.movieservice.domain.aggregate.genre;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "genres")
@Getter
@NoArgsConstructor
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Genre(String name, String description) {
        validate(name, description);
        this.name = normalizeRequired(name);
        this.description = normalizeNullable(description);
    }

    public void update(String name, String description) {
        validate(name, description);
        this.name = normalizeRequired(name);
        this.description = normalizeNullable(description);
    }

    private void validate(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Genre name must not be blank");
        }

        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("Genre name must not exceed 100 characters");
        }

        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("Genre description must not exceed 2000 characters");
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
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
