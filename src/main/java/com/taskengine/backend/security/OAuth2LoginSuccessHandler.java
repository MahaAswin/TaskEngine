package com.taskengine.backend.security;

import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import com.taskengine.backend.dto.AuthResponse;
import com.taskengine.backend.exception.BadRequestException;
import com.taskengine.backend.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final AuthService authService;
  private final AuthCookies authCookies;

  @Value("${app.frontend.base-url:http://localhost:5173}")
  private String frontendBaseUrl;

  @Override
  public void onAuthenticationSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException, ServletException {
    OAuth2User principal = (OAuth2User) authentication.getPrincipal();
    String sub =
        (principal instanceof OidcUser ou && ou.getSubject() != null)
            ? ou.getSubject()
            : principal.getAttribute("sub");
    String email = principal.getAttribute("email");
    String name = principal.getAttribute("name");
    String picture = principal.getAttribute("picture");
    Map<String, Object> attrs = principal.getAttributes();

    log.info(
        "OAuth login: email={}, name={}, sub={}, attrKeys={}",
        email,
        name,
        sub,
        attrs != null ? attrs.keySet() : null);

    if (email == null || email.isBlank()) {
      throw new BadRequestException("Google account has no email");
    }
    if (sub == null || sub.isBlank()) {
      throw new BadRequestException("Google account subject is missing");
    }

    AuthResponse tokens = authService.oauthLogin(sub, email, name, picture);
    authCookies.addTokens(response, tokens.getAccessToken(), tokens.getRefreshToken());
    String redirectUrl =
        UriComponentsBuilder.fromUriString(frontendBaseUrl)
            .path("/login")
            .queryParam("oauth", "success")
            .queryParam("accessToken", tokens.getAccessToken())
            .build()
            .toUriString();
    log.info("OAuth2 login success for email={}", email);
    response.sendRedirect(redirectUrl);
  }
}
