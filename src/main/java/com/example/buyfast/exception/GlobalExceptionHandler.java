//package com.example.buyfast.exception;
//
//import com.example.buyfast.common.dto.ApiResponse;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.LocalDateTime;
//
//@RestControllerAdvice(basePackages = "com.example.buyfast.modules")
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException e) {
//        ApiResponse<Object> response = ApiResponse.builder()
//                .message(e.getMessage())
//                .status(HttpStatus.NOT_FOUND.value())
//                .timestamp(LocalDateTime.now())
//                .build();
//        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
//    }
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException e) {
//        ApiResponse<Object> response = ApiResponse.builder()
//                .message(e.getMessage())
//                .status(HttpStatus.BAD_REQUEST.value())
//                .timestamp(LocalDateTime.now())
//                .build();
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }
//}
