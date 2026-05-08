package com.logobing.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;
    private Double wallet;
    
    public static <T> ApiResponse<T> success(String message, T data, Double wallet) {
        return ApiResponse.<T>builder().status("success").message(message).data(data).wallet(wallet).build();
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder().status("error").message(message).build();
    }
}
