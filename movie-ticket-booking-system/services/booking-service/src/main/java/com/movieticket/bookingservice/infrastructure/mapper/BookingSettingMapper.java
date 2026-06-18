package com.movieticket.bookingservice.infrastructure.mapper;

import com.movieticket.bookingservice.domain.entity.BookingSetting;
import com.movieticket.bookingservice.infrastructure.jpa.BookingSettingJpaEntity;

public class BookingSettingMapper {

    public static BookingSetting toDomain(BookingSettingJpaEntity entity) {
        if (entity == null) {
            return null;
        }
        return BookingSetting.builder()
                .id(entity.getId())
                .settingKey(entity.getSettingKey())
                .settingValue(entity.getSettingValue())
                .description(entity.getDescription())
                .build();
    }

    public static BookingSettingJpaEntity toEntity(BookingSetting domain) {
        if (domain == null) {
            return null;
        }
        BookingSettingJpaEntity entity = new BookingSettingJpaEntity();
        entity.setId(domain.getId());
        entity.setSettingKey(domain.getSettingKey());
        entity.setSettingValue(domain.getSettingValue());
        entity.setDescription(domain.getDescription());
        return entity;
    }
}
