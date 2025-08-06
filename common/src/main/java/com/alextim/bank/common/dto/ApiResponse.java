package com.alextim.bank.common.dto;

import lombok.*;

@AllArgsConstructor
@Getter
@ToString
public class ApiResponse<T> {

    private String status;

    private T data;

    private ApiError error;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", data, null);
    }

    public static <T> ApiResponse<T> error(String message, String details) {
        return new ApiResponse<>("ERROR", null, new ApiError(message, details));
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public static class ApiError {
        private String message;
        private String details;
    }
}
