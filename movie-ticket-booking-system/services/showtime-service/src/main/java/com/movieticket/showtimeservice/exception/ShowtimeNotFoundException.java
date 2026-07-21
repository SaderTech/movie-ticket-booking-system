package com.movieticket.showtimeservice.exception;




public class ShowtimeNotFoundException extends RuntimeException {


    public ShowtimeNotFoundException(Long id) {

        super("Showtime not found with id: " + id);

    }
}