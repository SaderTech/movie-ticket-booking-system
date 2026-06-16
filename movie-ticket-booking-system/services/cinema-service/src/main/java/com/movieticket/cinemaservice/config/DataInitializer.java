package com.movieticket.cinemaservice.config;

import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initSeatTypes(SeatTypeRepository seatTypeRepository) {
        return args -> {
            createSeatTypeIfNotExists(
                    seatTypeRepository,
                    "STANDARD",
                    "Standard Seat",
                    "Normal cinema seat"
            );

            createSeatTypeIfNotExists(
                    seatTypeRepository,
                    "VIP",
                    "VIP Seat",
                    "Premium seat"
            );

            createSeatTypeIfNotExists(
                    seatTypeRepository,
                    "COUPLE",
                    "Couple Seat",
                    "Seat for two people"
            );

            createSeatTypeIfNotExists(
                    seatTypeRepository,
                    "WHEELCHAIR",
                    "Wheelchair Seat",
                    "Accessible position for wheelchair users"
            );
        };
    }

    private void createSeatTypeIfNotExists(
            SeatTypeRepository seatTypeRepository,
            String code,
            String name,
            String description
    ) {
        if (!seatTypeRepository.existsByCodeIgnoreCase(code)) {
            seatTypeRepository.save(new SeatType(code, name, description));
        }
    }
}