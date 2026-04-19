package com.taskengine.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

  private final String accessToken;
  private final String refreshToken;
  /** Access token lifetime in seconds */
  private final long expiresIn;
}
