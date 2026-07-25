package com.movieticket.bookingservice.application.usecase;

public interface ReleaseSeatHoldUseCase {
    void execute(String holdToken, Long userId);
}
