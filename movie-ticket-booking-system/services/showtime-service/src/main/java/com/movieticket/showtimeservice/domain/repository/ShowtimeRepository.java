package com.movieticket.showtimeservice.domain.repository;


import com.movieticket.showtimeservice.domain.model.Showtime;
import com.movieticket.showtimeservice.domain.model.ShowtimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    // Lấy tất cả suất chiếu theo phim
    List<Showtime> findByMovieId(Long movieId);

    // Lấy tất cả suất chiếu theo rạp
    List<Showtime> findByCinemaId(Long cinemaId);

    // Lấy suất chiếu theo phòng
    List<Showtime> findByRoomId(Long roomId);

    // Lấy suất chiếu theo ngày
    List<Showtime> findByShowDate(LocalDate showDate);

    // Lấy theo trạng thái
    List<Showtime> findByStatus(ShowtimeStatus status);

    // Lấy theo phim và ngày
    List<Showtime> findByMovieIdAndShowDate(
            Long movieId,
            LocalDate showDate
    );

    // Lấy theo rạp và ngày
    List<Showtime> findByCinemaIdAndShowDate(
            Long cinemaId,
            LocalDate showDate
    );

    // Lấy theo phòng và ngày
    List<Showtime> findByRoomIdAndShowDate(
            Long roomId,
            LocalDate showDate
    );

    // Kiểm tra trùng giờ trong cùng phòng
    boolean existsByRoomIdAndShowDateAndStartTime(
            Long roomId,
            LocalDate showDate,
            LocalTime startTime
    );

    // Lấy suất chiếu còn vé theo phim và ngày
    List<Showtime> findByMovieIdAndShowDateAndStatus(
            Long movieId,
            LocalDate showDate,
            ShowtimeStatus status
    );

}