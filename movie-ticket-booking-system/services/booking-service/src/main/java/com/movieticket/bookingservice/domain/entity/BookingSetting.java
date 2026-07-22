package com.movieticket.bookingservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_settings",
       uniqueConstraints = {
           @UniqueConstraint(name = "uc_booking_settings_key", columnNames = {"setting_key"})
       }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;

    @Column(name = "setting_value", nullable = false, length = 255)
    private String settingValue;

    @Column(name = "description", length = 255)
    private String description;
}