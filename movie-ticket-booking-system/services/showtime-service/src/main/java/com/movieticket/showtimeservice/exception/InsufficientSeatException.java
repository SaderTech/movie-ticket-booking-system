package com.movieticket.showtimeservice.exception;


public class InsufficientSeatException extends RuntimeException {


    public InsufficientSeatException() {

        super("Not enough seats available");

    }

}