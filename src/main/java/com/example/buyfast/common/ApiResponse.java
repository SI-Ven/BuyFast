package com.example.buyfast.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * A standardized generic API response.
 * @param <T> The type of the payload.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL) // Omits null fields (like 'payload' in error responses)
public class ApiResponse<T> {

    // Fields define the constructor order for @AllArgsConstructor
    private final String message;
    private final T payload;
    private final int status;
    private final LocalDateTime timestamp;

    // --- Success Methods (with payload) ---

    /**
     * Creates a success response with a payload and custom message. (HTTP 200)
     */
    public static <T> ApiResponse<T> success(String message, T payload) {
        // CORRECTED ORDER: (message, payload, status, timestamp)
        return new ApiResponse<>(message, payload, HttpStatus.OK.value(), LocalDateTime.now());
    }

    /**
     * Creates a success response with a payload and default "Success" message. (HTTP 200)
     */
    public static <T> ApiResponse<T> success(T payload) {
        return success("Success", payload);
    }

    /**
     * Creates a "Created" response with a payload and custom message. (HTTP 201)
     */
    public static <T> ApiResponse<T> created(String message, T payload) {
        // CORRECTED ORDER: (message, payload, status, timestamp)
        return new ApiResponse<>(message, payload, HttpStatus.CREATED.value(), LocalDateTime.now());
    }

    // --- Success Methods (no payload) ---

    /**
     * Creates a success response with a custom message and no payload. (HTTP 200)
     */
    public static <T> ApiResponse<T> ok(String message) {
        // CORRECTED ORDER: (message, payload, status, timestamp)
        return new ApiResponse<>(message, null, HttpStatus.OK.value(), LocalDateTime.now());
    }

    /**
     * Creates a "Created" response with a custom message and no payload. (HTTP 201)
     */
    public static <T> ApiResponse<T> created(String message) {
        // CORRECTED ORDER: (message, payload, status, timestamp)
        return new ApiResponse<>(message, null, HttpStatus.CREATED.value(), LocalDateTime.now());
    }


    // --- Error Methods ---

    /**
     * Creates an error response.
     */
    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        // CORRECTED ORDER: (message, payload, status, timestamp)
        return new ApiResponse<>(message, null, status.value(), LocalDateTime.now());
    }
}