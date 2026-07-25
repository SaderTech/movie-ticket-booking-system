package com.movieticket.bookingservice.api.controller;

import com.movieticket.bookingservice.api.dto.*;
import com.movieticket.bookingservice.api.exception.ApiException;
import com.movieticket.bookingservice.api.exception.ErrorCode;
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
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {

    private final HoldSeatsUseCase holdSeatsUseCase;
    private final ReleaseSeatHoldUseCase releaseSeatHoldUseCase;
    private final ConfirmBookingUseCase confirmBookingUseCase;
    private final CancelBookingUseCase cancelBookingUseCase;
    private final GetBookingUseCase getBookingUseCase;
    private final GetMyBookingsUseCase getMyBookingsUseCase;
    private final GetSeatAvailabilityUseCase getSeatAvailabilityUseCase;
    private final BookingContext bookingContext;
    private final MovieClient movieClient;

    private Long getCurrentUserIdOrThrow() {
        Long userId = bookingContext.getCurrentUserId();
        if (userId == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, 401, "Missing or invalid user authentication");
        }
        return userId;
    }

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
        Long userId = getCurrentUserIdOrThrow();
        if (idempotencyKey != null) {
            log.debug("hold-seats request with Idempotency-Key: {}", idempotencyKey);
        }
        HoldSeatsCommand command = HoldSeatsCommand.builder()
                .userId(userId)
                .showtimeId(request.getShowtimeId())
                .seatCodes(request.getSeatCodes())
                .idempotencyKey(idempotencyKey)
                .build();
        HoldSeatsResponse response = holdSeatsUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Seats held successfully", response));
    }

    @PostMapping("/holds/{holdToken}/release")
    public ResponseEntity<ApiResponse<Void>> releaseSeatHold(@PathVariable String holdToken) {
        releaseSeatHoldUseCase.execute(holdToken, getCurrentUserIdOrThrow());
        return ResponseEntity.ok(ApiResponse.success("Seat hold released", null));
    }

    @GetMapping("/showtimes/{showtimeId}/seat-availability")
    public ResponseEntity<ApiResponse<SeatAvailabilityResponse>> getSeatAvailability(
            @PathVariable Long showtimeId) {
        getCurrentUserIdOrThrow();
        return ResponseEntity.ok(ApiResponse.success(getSeatAvailabilityUseCase.findByShowtimeId(showtimeId)));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ApiResponse<BookingResponse>> confirmBooking(
            @Valid @RequestBody ConfirmBookingRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserIdOrThrow();
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
                .idempotencyKey(idempotencyKey)
                .build();
        BookingResponse response = confirmBookingUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.success("Redirect to VNPay for payment", response));
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
        String message = "CONFIRMED".equals(response.getStatus())
                ? "Payment successful, booking confirmed"
                : "Payment processed, booking status: " + response.getStatus();
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    @PostMapping("/{bookingCode}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(
            @PathVariable String bookingCode,
            @RequestHeader(name = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody(required = false) CancelBookingRequest request) {
        Long userId = getCurrentUserIdOrThrow();
        String reason = request != null ? request.getReason() : null;
        CancelBookingCommand command = CancelBookingCommand.builder()
                .userId(userId)
                .bookingCode(bookingCode)
                .reason(reason)
                .idempotencyKey(idempotencyKey)
                .build();
        BookingResponse response = cancelBookingUseCase.execute(command);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", response));
    }

    @GetMapping("/{bookingCode}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable String bookingCode) {
        Long userId = getCurrentUserIdOrThrow();
        BookingResponse response = getBookingUseCase.findByBookingCode(bookingCode, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<PagedResponse<BookingResponse>>> getMyBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (page < 0) throw new ApiException(ErrorCode.INVALID_REQUEST, "Page must be >= 0");
        if (size < 1 || size > 100) throw new ApiException(ErrorCode.INVALID_REQUEST, "Size must be between 1 and 100");
        Long userId = getCurrentUserIdOrThrow();
        PagedResponse<BookingResponse> responses = getMyBookingsUseCase.findByUserId(userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
