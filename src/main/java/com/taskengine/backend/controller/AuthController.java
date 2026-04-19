package com.taskengine.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.taskengine.backend.dto.*;
import com.taskengine.backend.security.AuthCookies;
import com.taskengine.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

  private final AuthService authService;
  private final AuthCookies authCookies;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Register organization and admin user")
  public AuthResponse register(
      @Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
    AuthResponse body = authService.register(request);
    authCookies.addTokens(response, body.getAccessToken(), body.getRefreshToken());
    return body;
  }

  @PostMapping("/login")
  @Operation(summary = "Login with email and password")
  public AuthResponse login(
      @Valid @RequestBody AuthRequest request, HttpServletResponse response) {
    AuthResponse body = authService.login(request);
    authCookies.addTokens(response, body.getAccessToken(), body.getRefreshToken());
    return body;
  }

  @PostMapping("/refresh")
  @Operation(summary = "Rotate refresh token")
  public AuthResponse refresh(
      @RequestBody(required = false) RefreshRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse response) {
    String rt =
        request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()
            ? request.getRefreshToken()
            : authCookies.readRefreshToken(httpRequest);
    AuthResponse body = authService.refreshWithToken(rt);
    authCookies.addTokens(response, body.getAccessToken(), body.getRefreshToken());
    return body;
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Logout and revoke refresh token")
  public void logout(
      @RequestBody(required = false) LogoutRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse response) {
    String rt =
        request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()
            ? request.getRefreshToken()
            : authCookies.readRefreshToken(httpRequest);
    authService.logoutWithToken(rt);
    authCookies.clear(response);
  }

  @PostMapping("/oauth2/callback")
  @Operation(summary = "Google ID token exchange")
  public AuthResponse oauth2Callback(
      @Valid @RequestBody OAuth2CallbackRequest request, HttpServletResponse response) {
    AuthResponse body = authService.oauthCallback(request);
    authCookies.addTokens(response, body.getAccessToken(), body.getRefreshToken());
    return body;
  }
}
