package com.movieticket.bookingservice.infrastructure.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "booking_settings",
       uniqueConstraints = {
           @UniqueConstraint(name = "uc_booking_settings_key", columnNames = {"setting_key"})
       }
)
@Getter
@Setter
public class BookingSettingJpaEntity {

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
