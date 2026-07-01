package com.movieticket.movieservice.domain.aggregate.actor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "actors")
@Getter
@Setter
@NoArgsConstructor
public class Actor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String biography;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Actor(String name, String avatarUrl, String biography, LocalDate birthDate) {
        validateName(name);
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.biography = biography;
        this.birthDate = birthDate;
    }

    public void update(String name, String avatarUrl, String biography, LocalDate birthDate) {
        validateName(name);
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.biography = biography;
        this.birthDate = birthDate;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Actor name must not be blank");
        }
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}