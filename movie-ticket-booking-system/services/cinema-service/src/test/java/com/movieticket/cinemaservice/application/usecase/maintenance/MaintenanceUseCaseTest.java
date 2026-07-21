package com.movieticket.cinemaservice.application.usecase.maintenance;

import com.movieticket.cinemaservice.application.dto.request.CreateHallMaintenanceRequest;
import com.movieticket.cinemaservice.application.dto.response.HallMaintenanceResponse;
import com.movieticket.cinemaservice.application.exception.BusinessException;
import com.movieticket.cinemaservice.application.usecase.maintenance.CreateMaintenanceUseCase;
import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.aggregate.hall.HallMaintenance;
import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import com.movieticket.cinemaservice.domain.enums.HallStatus;
import com.movieticket.cinemaservice.domain.enums.HallType;
import com.movieticket.cinemaservice.domain.enums.MaintenanceStatus;
import com.movieticket.cinemaservice.infrastructure.repository.HallMaintenanceRepository;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceUseCaseTest {

    @Mock
    private HallRepository hallRepository;
    @Mock
    private HallMaintenanceRepository hallMaintenanceRepository;

    private CreateMaintenanceUseCase createMaintenanceUseCase;
    private Hall hall;

    @BeforeEach
    void setUp() {
        createMaintenanceUseCase = new CreateMaintenanceUseCase(hallRepository, hallMaintenanceRepository);
        hall = hall();
        lenient().when(hallRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(hall));
        lenient().when(hallMaintenanceRepository.save(any(HallMaintenance.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void rejectsStartTimeEqualToOrAfterEndTime() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        CreateHallMaintenanceRequest request = new CreateHallMaintenanceRequest(10L, start, start, "Cleaning");

        assertThrows(BusinessException.class, () -> createMaintenanceUseCase.execute(request));
    }

    @Test
    void rejectsMaintenanceStartingInPast() {
        CreateHallMaintenanceRequest request = new CreateHallMaintenanceRequest(
                10L, LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(1), "Cleaning"
        );

        assertThrows(BusinessException.class, () -> createMaintenanceUseCase.execute(request));
    }

    @Test
    void rejectsOverlappingMaintenance() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        HallMaintenance existing = new HallMaintenance(hall, start.minusMinutes(30), end.minusMinutes(30), "Repair");
        when(hallMaintenanceRepository.findOverlappingMaintenances(
                10L, start, end, MaintenanceStatus.CANCELLED
        )).thenReturn(List.of(existing));

        assertThrows(BusinessException.class, () -> createMaintenanceUseCase.execute(
                new CreateHallMaintenanceRequest(10L, start, end, "Cleaning")
        ));
    }

    @Test
    void allowsNewScheduleWhenCancelledSchedulesAreExcluded() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        LocalDateTime end = start.plusHours(2);
        when(hallMaintenanceRepository.findOverlappingMaintenances(
                10L, start, end, MaintenanceStatus.CANCELLED
        )).thenReturn(List.of());

        HallMaintenanceResponse response = createMaintenanceUseCase.execute(
                new CreateHallMaintenanceRequest(10L, start, end, "Cleaning")
        );

        assertEquals(MaintenanceStatus.SCHEDULED, response.status());
    }

    @Test
    void rejectsInvalidStatusTransitions() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        HallMaintenance maintenance = new HallMaintenance(hall, start, start.plusHours(1), "Repair");
        maintenance.changeStatus(MaintenanceStatus.IN_PROGRESS);
        maintenance.changeStatus(MaintenanceStatus.COMPLETED);

        assertThrows(
                IllegalArgumentException.class,
                () -> maintenance.changeStatus(MaintenanceStatus.SCHEDULED)
        );
        assertEquals(MaintenanceStatus.COMPLETED, maintenance.getStatus());
    }

    private Hall hall() {
        Cinema cinema = new Cinema("Cinema", "Address", "City", "0901234567", null, null, CinemaStatus.ACTIVE);
        ReflectionTestUtils.setField(cinema, "id", 1L);
        Hall hall = new Hall(cinema, "Hall", 100, HallType.STANDARD, HallStatus.ACTIVE);
        ReflectionTestUtils.setField(hall, "id", 10L);
        return hall;
    }
}
