package com.movieticket.commonweb.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "result", "code", "message", "traceId", "data" })
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private ApiResponseResult result;
    private String code;
    private String message;
    private String traceId;
    private T data;

    // Trạng thái phản hồi khép kín
    public enum ApiResponseResult {
        OK, NG
    }

    // Các hàm tiện ích tạo nhanh Response Thành công
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .result(ApiResponseResult.OK)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .result(ApiResponseResult.OK)
                .message(message)
                .data(data)
                .build();
    }
}
