package com.triple.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Getter
public class CommonResponse<T> {

    private String message;
    private T data;
    private String timestamp;

    public static <T> ResponseEntity<CommonResponse<T>> created (String message) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new CommonResponse<>(message, null, getCurrentTimestamp()));
    }

    public static <T> ResponseEntity<CommonResponse<T>> created (String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new CommonResponse<>(message, data, getCurrentTimestamp()));
    }

    public static <T> ResponseEntity<CommonResponse<T>> ok(String message) {
        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(message, getCurrentTimestamp()));
    }

    public static <T> ResponseEntity<CommonResponse<T>> ok(String message, T data) {
        return ResponseEntity.status(HttpStatus.OK).body(new CommonResponse<>(message, data, getCurrentTimestamp()));
    }

    private static String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static <T> ResponseEntity<CommonResponse<T>> error(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CommonResponse<>(message, null, getCurrentTimestamp()));
    }

    public static <T> ResponseEntity<CommonResponse<T>> error(String message, HttpStatus status) {
        return ResponseEntity.status(status).body(new CommonResponse<>(message, null, getCurrentTimestamp()));
    }

    public CommonResponse(String message, String timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }
}