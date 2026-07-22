package com.movieticket.cinemaservice.domain.aggregate.hall;

import com.movieticket.cinemaservice.domain.enums.MaintenanceStatus;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "hall_maintenance")
@Getter
@Setter
@NoArgsConstructor
public class HallMaintenance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hall_id", nullable = false)
    private Hall hall;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public HallMaintenance(Hall hall, LocalDateTime startTime, LocalDateTime endTime, String reason) {
        validateHall(hall);
        validateTime(startTime, endTime);

        this.hall = hall;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
        this.status = MaintenanceStatus.SCHEDULED;
    }

    public void changeStatus(MaintenanceStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Maintenance status must not be null");
        }

        if (this.status == status) {
            return;
        }

        boolean validTransition = switch (this.status) {
            case SCHEDULED -> status == MaintenanceStatus.IN_PROGRESS
                    || status == MaintenanceStatus.CANCELLED;
            case IN_PROGRESS -> status == MaintenanceStatus.COMPLETED
                    || status == MaintenanceStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };

        if (!validTransition) {
            throw new IllegalArgumentException(
                    "Invalid maintenance status transition from " + this.status + " to " + status
            );
        }
        this.status = status;
    }

    private void validateHall(Hall hall) {
        if (hall == null) {
            throw new IllegalArgumentException("Hall must not be null");
        }
    }

    private void validateTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Maintenance start time and end time are required");
        }

        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Maintenance start time must be before end time");
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
