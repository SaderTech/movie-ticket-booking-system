package com.movieticket.cinemaservice.application.usecase.maintenance;

import com.movieticket.cinemaservice.application.dto.request.UpdateMaintenanceStatusRequest;
import com.movieticket.cinemaservice.application.dto.response.HallMaintenanceResponse;
import com.movieticket.cinemaservice.application.exception.ResourceNotFoundException;
import com.movieticket.cinemaservice.domain.aggregate.hall.Hall;
import com.movieticket.cinemaservice.domain.aggregate.hall.HallMaintenance;
import com.movieticket.cinemaservice.domain.enums.HallStatus;
import com.movieticket.cinemaservice.domain.enums.MaintenanceStatus;
import com.movieticket.cinemaservice.infrastructure.repository.HallMaintenanceRepository;
import com.movieticket.cinemaservice.infrastructure.repository.HallRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateMaintenanceStatusUseCase {

    private final HallMaintenanceRepository hallMaintenanceRepository;
    private final HallRepository hallRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(
                    value = "maintenances",
                    allEntries = true
            ),
            @CacheEvict(
                    value = "halls",
                    allEntries = true
            )
    })
    public HallMaintenanceResponse execute(
            Long id,
            UpdateMaintenanceStatusRequest request
    ) {
        // Tìm lịch bảo trì
        HallMaintenance maintenance =
                hallMaintenanceRepository.findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Maintenance not found with id: " + id
                                )
                        );

        // Tìm và khóa phòng chiếu
        Long hallId = maintenance.getHall().getId();

        Hall hall = hallRepository.findByIdForUpdate(hallId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Hall not found with id: " + hallId
                        )
                );

        MaintenanceStatus newStatus = request.status();

        // Kiểm tra và thay đổi trạng thái lịch bảo trì
        maintenance.changeStatus(newStatus);

        // Bắt đầu thực hiện bảo trì
        if (newStatus == MaintenanceStatus.IN_PROGRESS) {
            hall.markAsMaintenance();
        }

        // Bảo trì hoàn thành hoặc bị hủy
        if (newStatus == MaintenanceStatus.COMPLETED
                || newStatus == MaintenanceStatus.CANCELLED) {

            boolean hasAnotherMaintenanceInProgress =
                    hallMaintenanceRepository
                            .existsByHall_IdAndStatusAndIdNot(
                                    hallId,
                                    MaintenanceStatus.IN_PROGRESS,
                                    maintenance.getId()
                            );

            /*
             * Chỉ chuyển về ACTIVE khi:
             * - Hall đang ở trạng thái MAINTENANCE.
             * - Không còn lịch bảo trì nào khác đang IN_PROGRESS.
             */
            if (hall.getStatus() == HallStatus.MAINTENANCE
                    && !hasAnotherMaintenanceInProgress) {
                hall.markAsActive();
            }
        }

        HallMaintenance savedMaintenance =
                hallMaintenanceRepository.save(maintenance);

        hallRepository.save(hall);

        return HallMaintenanceResponse.from(savedMaintenance);
    }
}