package com.movieticket.bookingservice.api.controller;

import com.movieticket.bookingservice.api.dto.*;
import com.movieticket.bookingservice.application.command.CancelBookingCommand;
import com.movieticket.bookingservice.application.command.ConfirmBookingCommand;
import com.movieticket.bookingservice.application.command.HoldSeatsCommand;
import com.movieticket.bookingservice.application.usecase.*;
import com.movieticket.bookingservice.infrastructure.client.MovieClient;
import com.movieticket.bookingservice.infrastructure.security.BookingContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final HoldSeatsUseCase holdSeatsUseCase;
    private final ConfirmBookingUseCase confirmBookingUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final GetBookingUseCase getBookingUseCase;
    private final GetMyBookingsUseCase getMyBookingsUseCase;
    private final BookingContext bookingContext;
    private final MovieClient movieClient;

    @GetMapping("/demo")
    public String testDemo(HttpServletRequest request) {
        String correlationId = request.getHeader("X-Correlation-ID");
        String userId = request.getHeader("X-User-ID");
        String userEmail = request.getHeader("X-User-Email");

        log.info("============== BOOKING SERVICE ==============");
        log.info("=> [1. Booking Service] Nhận request từ Gateway");
        log.info("=> Correlation ID: {}", correlationId);
        log.info("=> User ID: {}", userId);
        log.info("=> User Email: {}", userEmail);
        log.info("=> Đang gọi Movie Service bằng Feign Client...");
        log.info("=============================================");

        String result = movieClient.callMovieDemo();

        return "Hệ thống kết nối mượt mà! " + result;
    }

    @PostMapping("/hold-seats")
    public ResponseEntity<ApiResponse<HoldSeatsResponse>> holdSeats(
            @Valid @RequestBody HoldSeatsRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey) {
        Long userId = bookingContext.getCurrentUserId();
        if (idempotencyKey != null) {
            log.debug("hold-seats request with Idempotency-Key: {}", idempotencyKey);
        }
        HoldSeatsCommand command = HoldSeatsCommand.builder()
                .userId(userId)
                .showtimeId(request.getShowtimeId())
                .seatCodes(request.getSeatCodes())
                .build();
        HoldSeatsResponse response = holdSeatsUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seats held successfully", response));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @Valid @RequestBody ConfirmBookingRequest request,
            HttpServletRequest httpRequest) {
        Long userId = bookingContext.getCurrentUserId();
        String ipAddress = httpRequest.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = httpRequest.getRemoteAddr();
        }
        ConfirmBookingCommand command = ConfirmBookingCommand.builder()
                .userId(userId)
                .holdToken(request.getHoldToken())
                .paymentMethod(request.getPaymentMethod())
                .returnUrl(request.getReturnUrl())
                .ipAddress(ipAddress)
                .build();
        BookingResponse response = confirmBookingUseCase.execute(command);
        String msg = "VNPAY".equalsIgnoreCase(request.getPaymentMethod())
                ? "Redirect to VNPay for payment" : "Booking confirmed successfully";
        return ResponseEntity.ok(ApiResponse.success(msg, response));
    }

    @GetMapping("/vnpay-return")
    public ResponseEntity<ApiResponse<BookingResponse>> vnpayReturn(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values.length > 0) {
                params.put(key, values[0]);
            }
        });
        log.info("VNPay return params: {}", params);
        BookingResponse response = confirmBookingUseCase.handleVnPayReturn(params);
        return ResponseEntity.ok(ApiResponse.success("Payment successful, booking confirmed", response));
    }

    @PostMapping("/{bookingCode}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable String bookingCode,
            @RequestBody(required = false) CancelBookingRequest request) {
        Long userId = bookingContext.getCurrentUserId();
        String reason = request != null ? request.getReason() : null;
        CancelBookingCommand command = CancelBookingCommand.builder()
                .userId(userId)
                .bookingCode(bookingCode)
                .reason(reason)
                .build();
        BookingResponse response = cancelBookingUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }

    @GetMapping("/{bookingCode}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable String bookingCode) {
        BookingResponse response = getBookingUseCase.findByBookingCode(bookingCode);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> getMyBookings() {
        Long userId = bookingContext.getCurrentUserId();
        List<BookingResponse> responses = getMyBookingsUseCase.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
