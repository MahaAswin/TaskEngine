package com.taskengine.backend.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

  private boolean success = false;
  private String error;
  private String message;
  private List<FieldErrorDetail> details;

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class FieldErrorDetail {
    private String field;
    private String message;
  }
}
