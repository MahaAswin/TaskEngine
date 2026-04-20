package com.taskengine.backend.exception;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.taskengine.backend.api.ApiErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(PermissionDeniedException.class)
  public ResponseEntity<ApiErrorResponse> handlePermissionDenied(PermissionDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            ApiErrorResponse.builder()
                .error("PERMISSION_DENIED")
                .message(ex.getMessage())
                .build());
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            ApiErrorResponse.builder()
                .error("NOT_FOUND")
                .message(ex.getMessage())
                .build());
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
    List<ApiErrorResponse.FieldErrorDetail> details =
        ex.getConstraintViolations().stream()
            .map(
                v ->
                    new ApiErrorResponse.FieldErrorDetail(
                        v.getPropertyPath().toString(), v.getMessage()))
            .collect(Collectors.toList());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ApiErrorResponse.builder()
                .error("VALIDATION_ERROR")
                .message("Validation failed")
                .details(details)
                .build());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
    List<ApiErrorResponse.FieldErrorDetail> details =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                fe ->
                    new ApiErrorResponse.FieldErrorDetail(
                        fe.getField(), fe.getDefaultMessage()))
            .collect(Collectors.toList());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ApiErrorResponse.builder()
                .error("VALIDATION_ERROR")
                .message("Validation failed")
                .details(details)
                .build());
  }

  @ExceptionHandler({BadRequestException.class, BadCredentialsException.class, HttpMessageNotReadableException.class})
  public ResponseEntity<ApiErrorResponse> handleBadRequest(RuntimeException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(
            ApiErrorResponse.builder()
                .error("BAD_REQUEST")
                .message(ex.getMessage())
                .build());
  }

  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ApiErrorResponse> handleForbidden(ForbiddenException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(
            ApiErrorResponse.builder()
                .error("FORBIDDEN")
                .message(ex.getMessage())
                .build());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleOther(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ApiErrorResponse.builder()
                .error("INTERNAL_ERROR")
                .message("An unexpected error occurred")
                .build());
  }
}
