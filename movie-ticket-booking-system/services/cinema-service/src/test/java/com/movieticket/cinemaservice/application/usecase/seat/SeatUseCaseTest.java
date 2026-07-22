package com.movieticket.cinemaservice.application.usecase.seat;

import com.movieticket.cinemaservice.application.dto.request.CreateSeatRequest;
import com.movieticket.cinemaservice.application.dto.request.UpdateSeatRequest;
import com.movieticket.cinemaservice.application.dto.response.SeatResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.application.usecase.seat.CreateSeatUseCase;
import com.movieticket.cinemaservice.application.usecase.seat.UpdateSeatUseCase;
import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.aggregate.hall.Seat;
import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import com.movieticket.cinemaservice.domain.enums.HallStatus;
import com.movieticket.cinemaservice.domain.enums.HallType;
import com.movieticket.cinemaservice.domain.enums.SeatStatus;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import com.movieticket.cinemaservice.infrastructure.repository.SeatRepository;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatUseCaseTest {

    @Mock
    private HallRepository hallRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private SeatTypeRepository seatTypeRepository;

    private CreateSeatUseCase createSeatUseCase;
    private UpdateSeatUseCase updateSeatUseCase;
    private SeatType standardSeatType;

    @BeforeEach
    void setUp() {
        createSeatUseCase = new CreateSeatUseCase(hallRepository, seatRepository, seatTypeRepository);
        updateSeatUseCase = new UpdateSeatUseCase(seatRepository, seatTypeRepository);
        standardSeatType = new SeatType("STANDARD", "Standard", null);
        ReflectionTestUtils.setField(standardSeatType, "id", 5L);
        lenient().when(seatTypeRepository.findById(5L)).thenReturn(Optional.of(standardSeatType));
        lenient().when(seatRepository.save(any(Seat.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createsSeatSuccessfullyAndNormalizesRow() {
        Hall hall = hall(10L, 100);
        when(hallRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(hall));
        when(seatRepository.countByHall_Id(10L)).thenReturn(0L);
        when(seatRepository.existsByHall_IdAndRowNameIgnoreCaseAndSeatNumber(10L, "A", 1)).thenReturn(false);

        SeatResponse response = createSeatUseCase.execute(createRequest(10L));

        assertEquals("A", response.rowName());
        assertEquals(1, response.seatNumber());
    }

    @Test
    void rejectsDuplicateSeatInsideSameHall() {
        when(hallRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(hall(10L, 100)));
        when(seatRepository.countByHall_Id(10L)).thenReturn(1L);
        when(seatRepository.existsByHall_IdAndRowNameIgnoreCaseAndSeatNumber(10L, "A", 1)).thenReturn(true);

        assertThrows(BusinessException.class, () -> createSeatUseCase.execute(createRequest(10L)));
    }

    @Test
    void allowsSameRowAndNumberInDifferentHalls() {
        when(hallRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(hall(10L, 100)));
        when(hallRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(hall(20L, 100)));
        when(seatRepository.countByHall_Id(10L)).thenReturn(0L);
        when(seatRepository.countByHall_Id(20L)).thenReturn(0L);
        when(seatRepository.existsByHall_IdAndRowNameIgnoreCaseAndSeatNumber(10L, "A", 1)).thenReturn(false);
        when(seatRepository.existsByHall_IdAndRowNameIgnoreCaseAndSeatNumber(20L, "A", 1)).thenReturn(false);

        createSeatUseCase.execute(createRequest(10L));
        createSeatUseCase.execute(createRequest(20L));

        verify(seatRepository, times(1))
                .existsByHall_IdAndRowNameIgnoreCaseAndSeatNumber(10L, "A", 1);
        verify(seatRepository, times(1))
                .existsByHall_IdAndRowNameIgnoreCaseAndSeatNumber(20L, "A", 1);
    }

    @Test
    void rejectsSeatBeyondHallCapacity() {
        when(hallRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(hall(10L, 2)));
        when(seatRepository.countByHall_Id(10L)).thenReturn(2L);

        assertThrows(BusinessException.class, () -> createSeatUseCase.execute(createRequest(10L)));
    }

    @Test
    void updateWithMissingStatusDoesNotResetBrokenSeat() {
        Seat seat = new Seat(hall(10L, 100), standardSeatType, "A", 1, SeatStatus.BROKEN);
        ReflectionTestUtils.setField(seat, "id", 30L);
        when(seatRepository.findById(30L)).thenReturn(Optional.of(seat));
        when(seatRepository.findByHall_IdAndRowNameIgnoreCaseAndSeatNumber(10L, "A", 1))
                .thenReturn(Optional.of(seat));
        UpdateSeatRequest request = new UpdateSeatRequest(5L, "A", 1, null);

        assertThrows(IllegalArgumentException.class, () -> updateSeatUseCase.execute(30L, request));
        assertEquals(SeatStatus.BROKEN, seat.getStatus());
    }

    private CreateSeatRequest createRequest(Long hallId) {
        return new CreateSeatRequest(hallId, 5L, " a ", 1, SeatStatus.ACTIVE);
    }

    private Hall hall(Long id, int capacity) {
        Cinema cinema = new Cinema("Cinema", "Address", "City", "0901234567", null, null, CinemaStatus.ACTIVE);
        ReflectionTestUtils.setField(cinema, "id", 1L);
        Hall hall = new Hall(cinema, "Hall", capacity, HallType.STANDARD, HallStatus.ACTIVE);
        ReflectionTestUtils.setField(hall, "id", id);
        return hall;
    }
}
