package com.movieticket.commonweb.exception;

import com.movieticket.commonweb.base.ApiResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.stream.Collectors;

@Slf4j
public class BaseGlobalExceptionHandler {

    private static final String TRACE_ID_KEY = "traceId";

    // 1. Đánh chặn lỗi nghiệp vụ do các thành viên tự định nghĩa và throw ở service con
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ApiResponse<Void>> handleBaseException(@NonNull BaseException ex) {
        log.warn("Business Exception phát sinh: [{}] {}", ex.getErrorCode().code(), ex.getMessage());
        String traceId = MDC.get(TRACE_ID_KEY);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .result(ApiResponse.ApiResponseResult.NG)
                .code(ex.getErrorCode().code())
                .message(ex.getMessage())
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(ex.getErrorCode().status())
                .body(response);
    }

    // 2. Đánh chặn lỗi Validation (Ví dụ: Thiếu trường @NotBlank, @NotNull khi gửi request body)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String detailErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        log.warn("Dữ liệu Validation không hợp lệ: {}", detailErrors);
        String traceId = MDC.get(TRACE_ID_KEY);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .result(ApiResponse.ApiResponseResult.NG)
                .code("VALIDATION_ERROR")
                .message(detailErrors)
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    // 3. Đánh chặn lỗi hệ thống chưa được phân loại (Ví dụ: Sập mạng, NullPointer, lỗi Driver)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleSystemException(Exception ex) {
        log.error("Lỗi hệ thống nghiêm trọng (Unhandled Exception): ", ex);
        String traceId = MDC.get(TRACE_ID_KEY);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .result(ApiResponse.ApiResponseResult.NG)
                .code("INTERNAL_SERVER_ERROR")
                .message("Hệ thống gặp sự cố bất ngờ. Vui lòng thử lại sau.")
                .traceId(traceId)
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
