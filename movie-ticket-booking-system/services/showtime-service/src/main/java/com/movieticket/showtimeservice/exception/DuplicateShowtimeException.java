package com.movieticket.showtimeservice.exception;




public class DuplicateShowtimeException extends RuntimeException {


    public DuplicateShowtimeException() {

        super("Showtime already exists in this room at this time.");

    }

}