package com.triple.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class CommonResponse<T> {

    private String message;
    private T data;
    private String timestamp;

    public static <T> CommonResponse<T> created (String message) {
        return new CommonResponse<>(message, emptyData(), getCurrentTimestamp());
    }

    public static <T> CommonResponse<T> created (String message, T data) {
        return new CommonResponse<>(message, data != null ? data : emptyData(), getCurrentTimestamp());
    }

    public static <T> CommonResponse<T> ok(String message) {
        return new CommonResponse<>(message, emptyData(), getCurrentTimestamp());
    }

    public static <T> CommonResponse<T> ok(String message, T data) {
        return new CommonResponse<>(message, data != null ? data : emptyData(), getCurrentTimestamp());
    }

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T emptyData() {
        return (T) new Object();
    }
}