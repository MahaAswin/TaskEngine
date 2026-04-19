package com.taskengine.backend.security;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthCookies {

  public static final String ACCESS_TOKEN = "access_token";
  public static final String REFRESH_TOKEN = "refresh_token";

  @Value("${app.jwt.access-expiration-ms}")
  private long accessExpirationMs;

  @Value("${app.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  public void addTokens(HttpServletResponse response, String accessToken, String refreshToken) {
    response.addHeader("Set-Cookie", accessCookie(accessToken).toString());
    response.addHeader("Set-Cookie", refreshCookie(refreshToken).toString());
  }

  public void clear(HttpServletResponse response) {
    response.addHeader("Set-Cookie", clearCookie(ACCESS_TOKEN).toString());
    response.addHeader("Set-Cookie", clearCookie(REFRESH_TOKEN).toString());
  }

  public String readAccessToken(HttpServletRequest request) {
    return readCookie(request, ACCESS_TOKEN);
  }

  public String readRefreshToken(HttpServletRequest request) {
    return readCookie(request, REFRESH_TOKEN);
  }

  private ResponseCookie accessCookie(String value) {
    return ResponseCookie.from(ACCESS_TOKEN, value)
        .httpOnly(true)
        .secure(false)
        .path("/api")
        .maxAge(Duration.ofMillis(accessExpirationMs))
        .sameSite("Lax")
        .build();
  }

  private ResponseCookie refreshCookie(String value) {
    return ResponseCookie.from(REFRESH_TOKEN, value)
        .httpOnly(true)
        .secure(false)
        .path("/api")
        .maxAge(Duration.ofMillis(refreshExpirationMs))
        .sameSite("Lax")
        .build();
  }

  private ResponseCookie clearCookie(String name) {
    return ResponseCookie.from(name, "")
        .httpOnly(true)
        .path("/api")
        .maxAge(0)
        .build();
  }

  private static String readCookie(HttpServletRequest request, String name) {
    if (request.getCookies() == null) {
      return null;
    }
    for (Cookie c : request.getCookies()) {
      if (name.equals(c.getName())) {
        return c.getValue();
      }
    }
    return null;
  }
}
