package com.movieticket.bookingservice.application.usecase.impl;

import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
import com.movieticket.bookingservice.domain.entity.SeatHold;
import com.movieticket.bookingservice.domain.enums.SeatHoldStatus;
import com.movieticket.bookingservice.infrastructure.jpa.JpaBookingRepository;
import com.movieticket.bookingservice.infrastructure.jpa.JpaSeatHoldRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReleaseSeatHoldUseCaseImpl {

    private final JpaSeatHoldRepository seatHoldRepository;
    private final JpaBookingRepository bookingRepository;

    /**
     * Releases a hold abandoned before payment begins. The operation is
     * idempotent so navigation cleanup can safely call it more than once.
     */
    @Transactional
    public void execute(String holdToken, Long userId) {
        SeatHold hold = seatHoldRepository.findByHoldToken(holdToken)
                .orElseThrow(() -> new ApiException(ErrorCode.HOLD_NOT_FOUND,
                        "Seat hold not found: " + holdToken));

        if (!Objects.equals(hold.getUserId(), userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, 403,
                    "You can only release your own seat hold");
        }

        // A booking means the payment flow has started. Releasing the hold at
        // this point could invalidate a pending VNPay transaction.
        if (bookingRepository.findByHoldToken(holdToken).isPresent()) {
            throw new ApiException(ErrorCode.BOOKING_CANNOT_BE_CANCELLED, 409,
                    "Cannot release a hold after payment has been initiated");
        }

        if (hold.getStatus() == SeatHoldStatus.ACTIVE) {
            hold.release();
            seatHoldRepository.save(hold);
        }
    }
}
