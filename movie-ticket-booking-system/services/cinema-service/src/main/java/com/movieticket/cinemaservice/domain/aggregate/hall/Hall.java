package com.movieticket.cinemaservice.domain.aggregate.hall;

import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.domain.enums.HallStatus;
import com.movieticket.cinemaservice.domain.enums.HallType;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "halls",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_hall_cinema_name", columnNames = {"cinema_id", "name"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Hall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "hall_type", nullable = false, length = 30)
    private HallType hallType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private HallStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Seat> seats = new ArrayList<>();

    @OneToMany(mappedBy = "hall", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HallMaintenance> maintenances = new ArrayList<>();

    public Hall(Cinema cinema, String name, Integer capacity, HallType hallType, HallStatus status) {
        validateCinema(cinema);
        validateName(name);
        validateCapacity(capacity);

        this.cinema = cinema;
        this.name = name.trim();
        this.capacity = capacity;
        this.hallType = hallType == null ? HallType.STANDARD : hallType;
        this.status = status == null ? HallStatus.ACTIVE : status;
    }

    public void update(String name, Integer capacity, HallType hallType, HallStatus status) {
        validateName(name);
        validateCapacity(capacity);
        if (hallType == null) {
            throw new IllegalArgumentException("Hall type must not be null when updating");
        }
        if (status == null) {
            throw new IllegalArgumentException("Hall status must not be null when updating");
        }

        this.name = name.trim();
        this.capacity = capacity;
        this.hallType = hallType;
        this.status = status;
    }

    public void markAsMaintenance() {
        this.status = HallStatus.MAINTENANCE;
    }

    public void markAsActive() {
        this.status = HallStatus.ACTIVE;
    }

    private void validateCinema(Cinema cinema) {
        if (cinema == null) {
            throw new IllegalArgumentException("Cinema must not be null");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Hall name must not be blank");
        }
    }

    private void validateCapacity(Integer capacity) {
        if (capacity == null || capacity <= 0) {
            throw new IllegalArgumentException("Hall capacity must be greater than 0");
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
