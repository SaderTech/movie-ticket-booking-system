package com.movieticket.cinemaservice.application.usecase.cinema;

import com.movieticket.cinemaservice.application.dto.request.CreateCinemaRequest;
import com.movieticket.cinemaservice.application.dto.request.UpdateCinemaRequest;
import com.movieticket.cinemaservice.application.dto.response.CinemaResponse;
import com.movieticket.cinemaservice.application.usecase.cinema.CreateCinemaUseCase;
import com.movieticket.cinemaservice.application.usecase.cinema.UpdateCinemaUseCase;
import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import com.movieticket.cinemaservice.infrastructure.repository.CinemaRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CinemaUseCaseTest {

    @Mock
    private CinemaRepository cinemaRepository;

    private CreateCinemaUseCase createCinemaUseCase;
    private UpdateCinemaUseCase updateCinemaUseCase;
    private Validator validator;

    @BeforeEach
    void setUp() {
        createCinemaUseCase = new CreateCinemaUseCase(cinemaRepository);
        updateCinemaUseCase = new UpdateCinemaUseCase(cinemaRepository);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void createsCinemaSuccessfully() {
        CreateCinemaRequest request = validCreateRequest();
        when(cinemaRepository.existsByNameIgnoreCase("Galaxy Nguyen Du")).thenReturn(false);
        when(cinemaRepository.save(any(Cinema.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CinemaResponse response = createCinemaUseCase.execute(request);

        assertEquals("Galaxy Nguyen Du", response.name());
        assertEquals(CinemaStatus.ACTIVE, response.status());
    }

    @Test
    void rejectsBlankName() {
        CreateCinemaRequest request = new CreateCinemaRequest(
                " ", "116 Nguyen Du", "Ho Chi Minh City", "0901234567",
                new BigDecimal("10.7769"), new BigDecimal("106.6951"), CinemaStatus.ACTIVE
        );

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void rejectsCoordinatesOutsideAllowedRanges() {
        CreateCinemaRequest request = new CreateCinemaRequest(
                "Galaxy Nguyen Du", "116 Nguyen Du", "Ho Chi Minh City", "0901234567",
                new BigDecimal("90.1"), new BigDecimal("-180.1"), CinemaStatus.ACTIVE
        );

        assertEquals(2, validator.validate(request).size());
    }

    @Test
    void updateWithMissingStatusDoesNotResetInactiveCinema() {
        Cinema cinema = new Cinema(
                "Galaxy Nguyen Du", "116 Nguyen Du", "Ho Chi Minh City", "0901234567",
                new BigDecimal("10.7769"), new BigDecimal("106.6951"), CinemaStatus.INACTIVE
        );
        UpdateCinemaRequest request = new UpdateCinemaRequest(
                "Galaxy Nguyen Du", "116 Nguyen Du", "Ho Chi Minh City", "0901234567",
                new BigDecimal("10.7769"), new BigDecimal("106.6951"), null
        );
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));

        assertFalse(validator.validate(request).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> updateCinemaUseCase.execute(1L, request));
        assertEquals(CinemaStatus.INACTIVE, cinema.getStatus());
    }

    private CreateCinemaRequest validCreateRequest() {
        return new CreateCinemaRequest(
                " Galaxy Nguyen Du ",
                "116 Nguyen Du",
                "Ho Chi Minh City",
                "0901234567",
                new BigDecimal("10.7769"),
                new BigDecimal("106.6951"),
                CinemaStatus.ACTIVE
        );
    }
}
