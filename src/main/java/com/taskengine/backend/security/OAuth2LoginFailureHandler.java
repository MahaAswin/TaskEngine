package com.taskengine.backend.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

  @Value("${app.frontend.base-url:http://localhost:5173}")
  private String frontendBaseUrl;

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception)
      throws IOException, ServletException {
    String redirectUrl =
        UriComponentsBuilder.fromUriString(frontendBaseUrl)
            .path("/login")
            .queryParam("oauth", "error")
            .queryParam("message", "Google login failed")
            .build()
            .toUriString();
    response.sendRedirect(redirectUrl);
  }
}
