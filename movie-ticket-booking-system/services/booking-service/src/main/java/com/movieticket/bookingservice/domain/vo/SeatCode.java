package com.movieticket.bookingservice.domain.vo;

import java.util.List;
import java.util.stream.Collectors;

public record SeatCode(String code) {

    private static final String PATTERN = "^[A-Z]\\d+$";

    public SeatCode {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Seat code must not be blank");
        }
        if (!code.matches(PATTERN)) {
            throw new IllegalArgumentException("Invalid seat code format: " + code + " (expected e.g. A1, B12)");
        }
    }

    public static List<SeatCode> fromStrings(List<String> codes) {
        return codes.stream().map(SeatCode::new).collect(Collectors.toList());
    }

    public static List<String> toStrings(List<SeatCode> seatCodes) {
        return seatCodes.stream().map(SeatCode::code).collect(Collectors.toList());
    }
}
