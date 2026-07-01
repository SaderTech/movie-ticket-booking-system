package com.movieticket.cinemaservice.domain.aggregate.cinema;

import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cinemas")
@Getter
@Setter
@NoArgsConstructor
public class Cinema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 100)
    private String city;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CinemaStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cinema", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Hall> halls = new ArrayList<>();

    public Cinema(
            String name,
            String address,
            String city,
            String contactPhone,
            BigDecimal latitude,
            BigDecimal longitude,
            CinemaStatus status
    ) {
        validateName(name);
        this.name = name;
        this.address = address;
        this.city = city;
        this.contactPhone = contactPhone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status == null ? CinemaStatus.ACTIVE : status;
    }

    public void update(
            String name,
            String address,
            String city,
            String contactPhone,
            BigDecimal latitude,
            BigDecimal longitude,
            CinemaStatus status
    ) {
        validateName(name);
        this.name = name;
        this.address = address;
        this.city = city;
        this.contactPhone = contactPhone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status == null ? CinemaStatus.ACTIVE : status;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Cinema name must not be blank");
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