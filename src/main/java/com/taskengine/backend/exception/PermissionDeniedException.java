package com.taskengine.backend.exception;

public class PermissionDeniedException extends RuntimeException {

  public PermissionDeniedException(String message) {
    super(message);
  }
}
