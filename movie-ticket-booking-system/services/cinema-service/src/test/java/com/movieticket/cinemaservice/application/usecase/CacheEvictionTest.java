package com.movieticket.cinemaservice.application.usecase;

import com.movieticket.cinemaservice.application.dto.request.CreateSeatRequest;
import com.movieticket.cinemaservice.application.dto.request.UpdateCinemaRequest;
import com.movieticket.cinemaservice.application.dto.request.UpdateSeatTypeRequest;
import com.movieticket.cinemaservice.application.usecase.cinema.UpdateCinemaUseCase;
import com.movieticket.cinemaservice.application.usecase.seat.CreateSeatUseCase;
import com.movieticket.cinemaservice.application.usecase.seattype.UpdateSeatTypeUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CacheEvictionTest {

    @Test
    void creatingSeatEvictsSeatAndHallCaches() throws NoSuchMethodException {
        assertEquals(
                Set.of("seats", "halls"),
                evictedCaches(CreateSeatUseCase.class, CreateSeatRequest.class)
        );
    }

    @Test
    void updatingSeatTypeEvictsSeatTypeSeatAndHallCaches() throws NoSuchMethodException {
        assertEquals(
                Set.of("seat-types", "seats", "halls"),
                evictedCaches(UpdateSeatTypeUseCase.class, Long.class, UpdateSeatTypeRequest.class)
        );
    }

    @Test
    void updatingCinemaEvictsCinemaAndHallCaches() throws NoSuchMethodException {
        assertEquals(
                Set.of("cinemas", "halls"),
                evictedCaches(UpdateCinemaUseCase.class, Long.class, UpdateCinemaRequest.class)
        );
    }

    private Set<String> evictedCaches(Class<?> useCaseClass, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        Method method;
        method = useCaseClass.getMethod("execute", parameterTypes);

        Caching caching = method.getAnnotation(Caching.class);
        return Arrays.stream(caching.evict())
                .peek(cacheEvict -> assertEquals(true, cacheEvict.allEntries()))
                .map(CacheEvict::value)
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
    }
}
