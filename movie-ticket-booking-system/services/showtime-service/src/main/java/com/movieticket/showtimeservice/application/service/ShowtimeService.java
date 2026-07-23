package com.movieticket.showtimeservice.application.service;


import com.movieticket.showtimeservice.application.dto.request.CreateShowtimeRequest;
import com.movieticket.showtimeservice.application.dto.request.UpdateShowtimeRequest;
import com.movieticket.showtimeservice.application.dto.response.ShowtimeResponse;
import com.movieticket.showtimeservice.application.dto.request.ReserveSeatRequest;
import com.movieticket.showtimeservice.exception.InsufficientSeatException;
import com.movieticket.showtimeservice.application.dto.request.ReleaseSeatRequest;
import com.movieticket.showtimeservice.domain.model.Showtime;
import com.movieticket.showtimeservice.domain.repository.ShowtimeRepository;
import com.movieticket.showtimeservice.application.dto.response.SeatAvailabilityResponse;
import java.time.LocalDate;
import java.time.LocalTime;


import com.movieticket.showtimeservice.exception.DuplicateShowtimeException;
import com.movieticket.showtimeservice.exception.ShowtimeNotFoundException;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class ShowtimeService {


    private final ShowtimeRepository showtimeRepository;



    // ===============================
    // GET ALL SHOWTIMES
    // ===============================
    public List<ShowtimeResponse> getAllShowtimes() {

        return showtimeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();

    }



    // ===============================
    // GET SHOWTIME BY ID
    // ===============================
    public ShowtimeResponse getShowtimeById(Long id) {


        Showtime showtime =
                showtimeRepository.findById(id)
                        .orElseThrow(() ->
                                new ShowtimeNotFoundException(id)
                        );


        return mapToResponse(showtime);

    }




    // ===============================
    // CREATE SHOWTIME
    // ===============================
    public ShowtimeResponse createShowtime(
            CreateShowtimeRequest request
    ) {



        // Check ngày và thời gian hợp lệ
        validateShowDateTime(
                request.getShowDate(),
                request.getStartTime(),
                request.getEndTime()
        );



        // Check trùng lịch
        if (showtimeRepository
                .existsByRoomIdAndShowDateAndStartTime(
                        request.getRoomId(),
                        request.getShowDate(),
                        request.getStartTime()
                )) {


            throw new DuplicateShowtimeException();

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



        Showtime saved =
                showtimeRepository.save(showtime);



        return mapToResponse(saved);

    }





    // ===============================
    // UPDATE SHOWTIME
    // ===============================
    public ShowtimeResponse updateShowtime(
            Long id,
            UpdateShowtimeRequest request
    ) {



        Showtime showtime =
                showtimeRepository.findById(id)
                        .orElseThrow(() ->
                                new ShowtimeNotFoundException(id)
                        );



        validateShowDateTime(
                request.getShowDate(),
                request.getStartTime(),
                request.getEndTime()
        );



        showtime.setMovieId(
                request.getMovieId()
        );


        showtime.setCinemaId(
                request.getCinemaId()
        );


        showtime.setRoomId(
                request.getRoomId()
        );


        showtime.setShowDate(
                request.getShowDate()
        );


        showtime.setStartTime(
                request.getStartTime()
        );


        showtime.setEndTime(
                request.getEndTime()
        );


        showtime.setPrice(
                request.getPrice()
        );


        showtime.setAvailableSeats(
                request.getAvailableSeats()
        );


        showtime.setStatus(
                request.getStatus()
        );



        Showtime updated =
                showtimeRepository.save(showtime);



        return mapToResponse(updated);

    }






    // ===============================
    // DELETE SHOWTIME
    // ===============================
    public void deleteShowtime(Long id) {



        Showtime showtime =
                showtimeRepository.findById(id)
                        .orElseThrow(() ->
                                new ShowtimeNotFoundException(id)
                        );



        showtimeRepository.delete(showtime);

    }






    // ===============================
    // VALIDATE TIME
    // ===============================
    // ===============================
// VALIDATE SHOW DATE + TIME
// ===============================
    private void validateShowDateTime(
            LocalDate showDate,
            LocalTime startTime,
            LocalTime endTime
    ) {


        LocalDate today = LocalDate.now();


        // Không cho tạo suất chiếu trong quá khứ
        if(showDate.isBefore(today)) {

            throw new IllegalArgumentException(
                    "Show date must be today or future date"
            );

        }



        // Nếu chiếu trong hôm nay
        // thì giờ bắt đầu phải lớn hơn giờ hiện tại
        if(showDate.equals(today)
                && startTime.isBefore(LocalTime.now())) {


            throw new IllegalArgumentException(
                    "Start time must be in the future"
            );

        }



        // Check giờ kết thúc
        if(endTime.isBefore(startTime)
                || endTime.equals(startTime)) {


            throw new IllegalArgumentException(
                    "End time must be after start time"
            );

        }

    }

    // ========================================
// RESERVE SEATS
// ========================================
    public ShowtimeResponse reserveSeats(
            Long id,
            ReserveSeatRequest request
    ) {


        Showtime showtime =
                showtimeRepository.findById(id)
                        .orElseThrow(() ->
                                new ShowtimeNotFoundException(id)
                        );



        Integer currentSeats =
                showtime.getAvailableSeats();



        Integer reserveQuantity =
                request.getQuantity();



        if(currentSeats < reserveQuantity) {

            throw new InsufficientSeatException();

        }



        showtime.setAvailableSeats(
                currentSeats - reserveQuantity
        );



        // Nếu hết ghế thì đổi trạng thái
        if(showtime.getAvailableSeats() == 0){

            showtime.setStatus(
                    com.movieticket.showtimeservice.domain.model.ShowtimeStatus.FULL
            );

        }



        Showtime updated =
                showtimeRepository.save(showtime);



        return mapToResponse(updated);

    }







    // ===============================
    // ENTITY -> RESPONSE DTO
    // ===============================
    private ShowtimeResponse mapToResponse(
            Showtime showtime
    ) {


        return ShowtimeResponse.builder()

                .id(showtime.getId())

                .movieId(showtime.getMovieId())

                .cinemaId(showtime.getCinemaId())

                .roomId(showtime.getRoomId())

                .showDate(showtime.getShowDate())

                .startTime(showtime.getStartTime())

                .endTime(showtime.getEndTime())

                .price(showtime.getPrice())

                .availableSeats(
                        showtime.getAvailableSeats()
                )

                .status(
                        showtime.getStatus()
                )

                .build();

    }

    // ===============================
// GET SHOWTIMES BY MOVIE
// ===============================
    public List<ShowtimeResponse> getShowtimesByMovieId(
            Long movieId
    ) {

        return showtimeRepository
                .findByMovieId(movieId)
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    // ===============================
// GET SHOWTIMES BY CINEMA
// ===============================
    public List<ShowtimeResponse> getShowtimesByCinemaId(
            Long cinemaId
    ) {

        return showtimeRepository
                .findByCinemaId(cinemaId)
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    // ===============================
// GET SHOWTIMES BY DATE
// ===============================
    public List<ShowtimeResponse> getShowtimesByDate(
            java.time.LocalDate date
    ) {

        return showtimeRepository
                .findByShowDate(date)
                .stream()
                .map(this::mapToResponse)
                .toList();

    }


    // ========================================
// GET SHOWTIMES BY MOVIE AND DATE
// ========================================
    public List<ShowtimeResponse> getShowtimesByMovieAndDate(
            Long movieId,
            java.time.LocalDate date
    ) {


        return showtimeRepository
                .findByMovieIdAndShowDate(
                        movieId,
                        date
                )
                .stream()
                .map(this::mapToResponse)
                .toList();

    }


    // ========================================
    // GET AVAILABLE SHOWTIMES BY MOVIE + DATE
    // ========================================
    public List<ShowtimeResponse> getAvailableShowtimes(
            Long movieId,
            java.time.LocalDate date
    ) {


        return showtimeRepository
                .findByMovieIdAndShowDateAndStatus(
                        movieId,
                        date,
                        com.movieticket.showtimeservice.domain.model.ShowtimeStatus.AVAILABLE
                )
                .stream()
                .map(this::mapToResponse)
                .toList();

    }

    // ========================================
// RELEASE SEATS
// ========================================
    public ShowtimeResponse releaseSeats(
            Long id,
            ReleaseSeatRequest request
    ) {


        Showtime showtime =
                showtimeRepository.findById(id)
                        .orElseThrow(() ->
                                new ShowtimeNotFoundException(id)
                        );


        Integer currentSeats =
                showtime.getAvailableSeats();


        Integer releaseQuantity =
                request.getQuantity();



        showtime.setAvailableSeats(
                currentSeats + releaseQuantity
        );



        // Nếu trước đó hết ghế thì mở lại
        if(showtime.getStatus()
                == com.movieticket.showtimeservice.domain.model.ShowtimeStatus.FULL) {


            showtime.setStatus(
                    com.movieticket.showtimeservice.domain.model.ShowtimeStatus.AVAILABLE
            );

        }



        Showtime updated =
                showtimeRepository.save(showtime);



        return mapToResponse(updated);

    }

    // ========================================
// CHECK SEAT AVAILABILITY
// ========================================
    public SeatAvailabilityResponse checkAvailability(
            Long id,
            Integer quantity
    ) {


        Showtime showtime =
                showtimeRepository.findById(id)
                        .orElseThrow(() ->
                                new ShowtimeNotFoundException(id)
                        );



        boolean available =
                showtime.getAvailableSeats() >= quantity;



        return SeatAvailabilityResponse.builder()

                .showtimeId(
                        showtime.getId()
                )

                .requestedSeats(
                        quantity
                )

                .availableSeats(
                        showtime.getAvailableSeats()
                )

                .available(
                        available
                )

                .build();

    }

}