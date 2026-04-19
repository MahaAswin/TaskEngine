package com.taskengine.backend.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private boolean success = true;
  private T data;
  private String message;

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, data, null);
  }

  public static <T> ApiResponse<T> ok(T data, String message) {
    return new ApiResponse<>(true, data, message);
  }
}
