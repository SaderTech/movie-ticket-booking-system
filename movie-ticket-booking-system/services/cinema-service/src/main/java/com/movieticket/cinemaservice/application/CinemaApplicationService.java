package com.movieticket.cinemaservice.application;

import com.movieticket.cinemaservice.api.dto.request.CreateCinemaRequest;
import com.movieticket.cinemaservice.api.dto.request.CreateHallMaintenanceRequest;
import com.movieticket.cinemaservice.api.dto.request.CreateHallRequest;
import com.movieticket.cinemaservice.api.dto.request.CreateSeatRequest;
import com.movieticket.cinemaservice.api.dto.request.CreateSeatTypeRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateCinemaRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateHallRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateMaintenanceStatusRequest;
import com.movieticket.cinemaservice.api.dto.request.UpdateSeatRequest;
import com.movieticket.cinemaservice.api.dto.response.CinemaResponse;
import com.movieticket.cinemaservice.api.dto.response.HallMaintenanceResponse;
import com.movieticket.cinemaservice.api.dto.response.HallResponse;
import com.movieticket.cinemaservice.api.dto.response.SeatResponse;
import com.movieticket.cinemaservice.api.dto.response.SeatTypeResponse;
import com.movieticket.cinemaservice.api.exception.BusinessException;
import com.movieticket.cinemaservice.api.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.cinema.Cinema;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.aggregate.hall.HallMaintenance;
import com.movieticket.cinemaservice.domain.aggregate.hall.Seat;
import com.movieticket.cinemaservice.domain.aggregate.seattype.SeatType;
import com.movieticket.cinemaservice.domain.enums.CinemaStatus;
import com.movieticket.cinemaservice.domain.enums.MaintenanceStatus;
import com.movieticket.cinemaservice.infrastructure.repository.CinemaRepository;
import com.movieticket.cinemaservice.infrastructure.repository.HallMaintenanceRepository;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import com.movieticket.cinemaservice.infrastructure.repository.SeatRepository;
import com.movieticket.cinemaservice.infrastructure.repository.SeatTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CinemaApplicationService {

    private final CinemaRepository cinemaRepository;
    private final HallRepository hallRepository;
    private final SeatRepository seatRepository;
    private final SeatTypeRepository seatTypeRepository;
    private final HallMaintenanceRepository hallMaintenanceRepository;

    public CinemaResponse createCinema(CreateCinemaRequest request) {
        if (cinemaRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Cinema name already exists: " + request.name());
        }

        Cinema cinema = new Cinema(
                request.name(),
                request.address(),
                request.city(),
                request.contactPhone(),
                request.latitude(),
                request.longitude(),
                request.status()
        );

        return CinemaResponse.from(cinemaRepository.save(cinema));
    }

    @Transactional(readOnly = true)
    public List<CinemaResponse> getAllCinemas(CinemaStatus status) {
        if (status != null) {
            return cinemaRepository.findByStatus(status)
                    .stream()
                    .map(CinemaResponse::from)
                    .toList();
        }

        return cinemaRepository.findAll()
                .stream()
                .map(CinemaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CinemaResponse getCinemaById(Long id) {
        return CinemaResponse.from(findCinemaById(id));
    }

    public CinemaResponse updateCinema(Long id, UpdateCinemaRequest request) {
        Cinema cinema = findCinemaById(id);

        if (!cinema.getName().equalsIgnoreCase(request.name())
                && cinemaRepository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Cinema name already exists: " + request.name());
        }

        cinema.update(
                request.name(),
                request.address(),
                request.city(),
                request.contactPhone(),
                request.latitude(),
                request.longitude(),
                request.status()
        );

        return CinemaResponse.from(cinemaRepository.save(cinema));
    }

    public SeatTypeResponse createSeatType(CreateSeatTypeRequest request) {
        if (seatTypeRepository.existsByCodeIgnoreCase(request.code())) {
            throw new BusinessException("Seat type code already exists: " + request.code());
        }

        SeatType seatType = new SeatType(
                request.code(),
                request.name(),
                request.description()
        );

        return SeatTypeResponse.from(seatTypeRepository.save(seatType));
    }

    @Transactional(readOnly = true)
    public List<SeatTypeResponse> getAllSeatTypes() {
        return seatTypeRepository.findAll()
                .stream()
                .map(SeatTypeResponse::from)
                .toList();
    }

    public HallResponse createHall(CreateHallRequest request) {
        Cinema cinema = findCinemaById(request.cinemaId());

        if (hallRepository.existsByCinema_IdAndNameIgnoreCase(request.cinemaId(), request.name())) {
            throw new BusinessException("Hall name already exists in this cinema: " + request.name());
        }

        Hall hall = new Hall(
                cinema,
                request.name(),
                request.capacity(),
                request.hallType(),
                request.status()
        );

        return HallResponse.from(hallRepository.save(hall));
    }

    @Transactional(readOnly = true)
    public HallResponse getHallById(Long id) {
        return HallResponse.from(findHallById(id));
    }

    @Transactional(readOnly = true)
    public List<HallResponse> getHallsByCinemaId(Long cinemaId) {
        return hallRepository.findByCinema_Id(cinemaId)
                .stream()
                .map(HallResponse::from)
                .toList();
    }

    public HallResponse updateHall(Long id, UpdateHallRequest request) {
        Hall hall = findHallById(id);
        Long cinemaId = hall.getCinema().getId();

        hallRepository.findByCinema_IdAndNameIgnoreCase(cinemaId, request.name())
                .ifPresent(existingHall -> {
                    if (!existingHall.getId().equals(hall.getId())) {
                        throw new BusinessException("Hall name already exists in this cinema: " + request.name());
                    }
                });

        hall.update(
                request.name(),
                request.capacity(),
                request.hallType(),
                request.status()
        );

        return HallResponse.from(hallRepository.save(hall));
    }

    public SeatResponse createSeat(CreateSeatRequest request) {
        Hall hall = findHallById(request.hallId());
        SeatType seatType = findSeatTypeById(request.seatTypeId());

        if (seatRepository.existsByHall_IdAndRowNameIgnoreCaseAndSeatNumber(
                request.hallId(),
                request.rowName(),
                request.seatNumber()
        )) {
            throw new BusinessException("Seat already exists in this hall: "
                    + request.rowName() + request.seatNumber());
        }

        Seat seat = new Seat(
                hall,
                seatType,
                request.rowName(),
                request.seatNumber(),
                request.status()
        );

        return SeatResponse.from(seatRepository.save(seat));
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> getSeatsByHallId(Long hallId) {
        return seatRepository.findByHall_IdOrderByRowNameAscSeatNumberAsc(hallId)
                .stream()
                .map(SeatResponse::from)
                .toList();
    }

    public SeatResponse updateSeat(Long id, UpdateSeatRequest request) {
        Seat seat = findSeatById(id);
        SeatType seatType = findSeatTypeById(request.seatTypeId());
        Long hallId = seat.getHall().getId();

        seatRepository.findByHall_IdAndRowNameIgnoreCaseAndSeatNumber(
                hallId,
                request.rowName(),
                request.seatNumber()
        ).ifPresent(existingSeat -> {
            if (!existingSeat.getId().equals(seat.getId())) {
                throw new BusinessException("Seat already exists in this hall: "
                        + request.rowName() + request.seatNumber());
            }
        });

        seat.update(
                seatType,
                request.rowName(),
                request.seatNumber(),
                request.status()
        );

        return SeatResponse.from(seatRepository.save(seat));
    }

    public HallMaintenanceResponse createMaintenance(CreateHallMaintenanceRequest request) {
        Hall hall = findHallById(request.hallId());

        List<HallMaintenance> overlaps = hallMaintenanceRepository.findOverlappingMaintenances(
                request.hallId(),
                request.startTime(),
                request.endTime(),
                MaintenanceStatus.CANCELLED
        );

        if (!overlaps.isEmpty()) {
            throw new BusinessException("Maintenance time overlaps with existing schedule");
        }

        HallMaintenance maintenance = new HallMaintenance(
                hall,
                request.startTime(),
                request.endTime(),
                request.reason()
        );

        return HallMaintenanceResponse.from(hallMaintenanceRepository.save(maintenance));
    }

    @Transactional(readOnly = true)
    public List<HallMaintenanceResponse> getMaintenancesByHallId(Long hallId) {
        return hallMaintenanceRepository.findByHall_IdOrderByStartTimeDesc(hallId)
                .stream()
                .map(HallMaintenanceResponse::from)
                .toList();
    }

    public HallMaintenanceResponse updateMaintenanceStatus(
            Long id,
            UpdateMaintenanceStatusRequest request
    ) {
        HallMaintenance maintenance = findMaintenanceById(id);
        maintenance.changeStatus(request.status());

        return HallMaintenanceResponse.from(hallMaintenanceRepository.save(maintenance));
    }

    private Cinema findCinemaById(Long id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cinema not found with id: " + id));
    }

    private Hall findHallById(Long id) {
        return hallRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hall not found with id: " + id));
    }

    private Seat findSeatById(Long id) {
        return seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
    }

    private SeatType findSeatTypeById(Long id) {
        return seatTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat type not found with id: " + id));
    }

    private HallMaintenance findMaintenanceById(Long id) {
        return hallMaintenanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Maintenance not found with id: " + id));
    }
}