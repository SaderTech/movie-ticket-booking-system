package com.movieticket.showtimeservice.exception;


public class InvalidSeatReleaseException extends RuntimeException {


    public InvalidSeatReleaseException() {

        super("Released seats exceed total capacity");

    }

}