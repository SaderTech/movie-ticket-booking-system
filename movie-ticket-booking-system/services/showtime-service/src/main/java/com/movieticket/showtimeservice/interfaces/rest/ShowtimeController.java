package com.movieticket.showtimeservice.interfaces.rest;

import com.movieticket.showtimeservice.application.dto.request.CreateShowtimeRequest;
import com.movieticket.showtimeservice.application.dto.request.UpdateShowtimeRequest;
import com.movieticket.showtimeservice.application.dto.response.ShowtimeResponse;
import com.movieticket.showtimeservice.application.service.ShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    /**
     * Lấy tất cả suất chiếu
     */
    @GetMapping
    public ResponseEntity<List<ShowtimeResponse>> getAllShowtimes() {
        return ResponseEntity.ok(showtimeService.getAllShowtimes());
    }

    /**
     * Lấy suất chiếu theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> getShowtimeById(
            @PathVariable("id") Long id) {

        return ResponseEntity.ok(showtimeService.getShowtimeById(id));
    }

    /**
     * Tạo suất chiếu mới
     */
    @PostMapping
    public ResponseEntity<ShowtimeResponse> createShowtime(
            @Valid @RequestBody CreateShowtimeRequest request) {

        ShowtimeResponse response = showtimeService.createShowtime(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cập nhật suất chiếu
     */
    @PutMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> updateShowtime(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateShowtimeRequest request) {

        ShowtimeResponse response = showtimeService.updateShowtime(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa suất chiếu
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShowtime(
            @PathVariable("id") Long id) {

        showtimeService.deleteShowtime(id);
        return ResponseEntity.noContent().build();
    }
}