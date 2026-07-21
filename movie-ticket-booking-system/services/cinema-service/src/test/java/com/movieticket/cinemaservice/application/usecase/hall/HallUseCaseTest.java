package com.movieticket.cinemaservice.application.usecase.hall;

import com.movieticket.cinemaservice.application.dto.request.CreateHallRequest;
import com.movieticket.cinemaservice.application.dto.request.UpdateHallRequest;
import com.movieticket.cinemaservice.application.dto.response.HallDetailResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.application.usecase.hall.CreateHallUseCase;
import com.movieticket.cinemaservice.application.usecase.hall.UpdateHallUseCase;
import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import com.movieticket.cinemaservice.domain.enums.HallStatus;
import com.movieticket.cinemaservice.domain.enums.HallType;
import com.movieticket.cinemaservice.infrastructure.repository.CinemaRepository;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import com.movieticket.cinemaservice.infrastructure.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class HallUseCaseTest {

    @Mock
    private CinemaRepository cinemaRepository;
    @Mock
    private HallRepository hallRepository;
    @Mock
    private SeatRepository seatRepository;

    private CreateHallUseCase createHallUseCase;
    private UpdateHallUseCase updateHallUseCase;

    @BeforeEach
    void setUp() {
        createHallUseCase = new CreateHallUseCase(cinemaRepository, hallRepository);
        updateHallUseCase = new UpdateHallUseCase(hallRepository, seatRepository);
        lenient().when(hallRepository.save(any(Hall.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsHallSuccessfully() {
        Cinema cinema = cinema(1L, "Cinema One");
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema));
        when(hallRepository.existsByCinema_IdAndNameIgnoreCase(1L, "Hall 1")).thenReturn(false);

        HallDetailResponse response = createHallUseCase.execute(createRequest(1L));

        assertEquals("Hall 1", response.name());
        assertEquals(100, response.capacity());
    }

    @Test
    void rejectsDuplicateHallNameInsideSameCinema() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema(1L, "Cinema One")));
        when(hallRepository.existsByCinema_IdAndNameIgnoreCase(1L, "Hall 1")).thenReturn(true);

        assertThrows(BusinessException.class, () -> createHallUseCase.execute(createRequest(1L)));
    }

    @Test
    void allowsSameHallNameInDifferentCinemas() {
        when(cinemaRepository.findById(1L)).thenReturn(Optional.of(cinema(1L, "Cinema One")));
        when(cinemaRepository.findById(2L)).thenReturn(Optional.of(cinema(2L, "Cinema Two")));
        when(hallRepository.existsByCinema_IdAndNameIgnoreCase(1L, "Hall 1")).thenReturn(false);
        when(hallRepository.existsByCinema_IdAndNameIgnoreCase(2L, "Hall 1")).thenReturn(false);

        createHallUseCase.execute(createRequest(1L));
        createHallUseCase.execute(createRequest(2L));

        verify(hallRepository, times(1)).existsByCinema_IdAndNameIgnoreCase(1L, "Hall 1");
        verify(hallRepository, times(1)).existsByCinema_IdAndNameIgnoreCase(2L, "Hall 1");
    }

    @Test
    void rejectsCapacityBelowCurrentSeatCount() {
        Hall hall = new Hall(cinema(1L, "Cinema One"), "Hall 1", 120, HallType.STANDARD, HallStatus.ACTIVE);
        when(hallRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(hall));
        when(seatRepository.countByHall_Id(10L)).thenReturn(100L);
        UpdateHallRequest request = new UpdateHallRequest("Hall 1", 80, HallType.STANDARD, HallStatus.ACTIVE);

        assertThrows(BusinessException.class, () -> updateHallUseCase.execute(10L, request));
    }

    private CreateHallRequest createRequest(Long cinemaId) {
        return new CreateHallRequest(cinemaId, " Hall 1 ", 100, HallType.STANDARD, HallStatus.ACTIVE);
    }

    private Cinema cinema(Long id, String name) {
        Cinema cinema = new Cinema(name, "Address", "City", "0901234567", null, null, CinemaStatus.ACTIVE);
        ReflectionTestUtils.setField(cinema, "id", id);
        return cinema;
    }
}
