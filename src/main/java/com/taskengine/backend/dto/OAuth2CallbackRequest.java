package com.taskengine.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuth2CallbackRequest {

  /** Google ID token (JWT) from Google Identity Services */
  @NotBlank private String idToken;
}
