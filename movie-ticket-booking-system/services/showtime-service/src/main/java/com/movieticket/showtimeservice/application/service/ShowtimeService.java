package com.movieticket.showtimeservice.application.service;



import com.movieticket.showtimeservice.application.dto.request.CreateShowtimeRequest;
import com.movieticket.showtimeservice.application.dto.request.UpdateShowtimeRequest;
import com.movieticket.showtimeservice.application.dto.response.ShowtimeResponse;
import com.movieticket.showtimeservice.domain.model.Showtime;
import com.movieticket.showtimeservice.domain.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    // Lấy tất cả suất chiếu
    public List<ShowtimeResponse> getAllShowtimes() {
        return showtimeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    // Lấy suất chiếu theo ID
    public ShowtimeResponse getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Showtime not found with id: " + id));

        return mapToResponse(showtime);
    }

    // Thêm suất chiếu
    public ShowtimeResponse createShowtime(CreateShowtimeRequest request) {

        if (showtimeRepository.existsByRoomIdAndShowDateAndStartTime(
                request.getRoomId(),
                request.getShowDate(),
                request.getStartTime())) {

            throw new RuntimeException("Showtime already exists in this room at this time.");
        }

        Showtime showtime = Showtime.builder()
                .movieId(request.getMovieId())
                .cinemaId(request.getCinemaId())
                .roomId(request.getRoomId())
                .showDate(request.getShowDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .price(request.getPrice())
                .availableSeats(request.getAvailableSeats())
                .status(request.getStatus())
                .build();

        Showtime saved = showtimeRepository.save(showtime);

        return mapToResponse(saved);
    }

    // Cập nhật suất chiếu
    public ShowtimeResponse updateShowtime(Long id,
                                           UpdateShowtimeRequest request) {

        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Showtime not found with id: " + id));

        showtime.setMovieId(request.getMovieId());
        showtime.setCinemaId(request.getCinemaId());
        showtime.setRoomId(request.getRoomId());
        showtime.setShowDate(request.getShowDate());
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getEndTime());
        showtime.setPrice(request.getPrice());
        showtime.setAvailableSeats(request.getAvailableSeats());
        showtime.setStatus(request.getStatus());

        Showtime updated = showtimeRepository.save(showtime);

        return mapToResponse(updated);
    }

    // Xóa suất chiếu
    public void deleteShowtime(Long id) {

        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Showtime not found with id: " + id));

        showtimeRepository.delete(showtime);
    }

    // Chuyển Entity -> Response
    private ShowtimeResponse mapToResponse(Showtime showtime) {

        return ShowtimeResponse.builder()
                .id(showtime.getId())
                .movieId(showtime.getMovieId())
                .cinemaId(showtime.getCinemaId())
                .roomId(showtime.getRoomId())
                .showDate(showtime.getShowDate())
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .price(showtime.getPrice())
                .availableSeats(showtime.getAvailableSeats())
                .status(showtime.getStatus())
                .build();
    }
}