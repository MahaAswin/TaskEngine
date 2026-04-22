package com.taskengine.backend.security;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Forces Google to show the account chooser, preventing "always same account" logins caused by
 * silent SSO reuse.
 */
public class GooglePromptAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

  private final DefaultOAuth2AuthorizationRequestResolver delegate;

  public GooglePromptAuthorizationRequestResolver(
      ClientRegistrationRepository repo, String authorizationRequestBaseUri) {
    this.delegate = new DefaultOAuth2AuthorizationRequestResolver(repo, authorizationRequestBaseUri);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    // Spring may call this overload; ensure google requests are still customized.
    String uri = request.getRequestURI();
    if (uri != null && uri.endsWith("/google")) {
      return resolve(request, "google");
    }
    return delegate.resolve(request);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
    OAuth2AuthorizationRequest ar = delegate.resolve(request, clientRegistrationId);
    return customize(ar, clientRegistrationId);
  }

  private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest ar, String clientRegistrationId) {
    if (ar == null) return null;
    if (clientRegistrationId == null || !"google".equalsIgnoreCase(clientRegistrationId)) {
      return ar;
    }
    Map<String, Object> extra = new LinkedHashMap<>(ar.getAdditionalParameters());
    extra.putIfAbsent("prompt", "select_account");
    // extra.putIfAbsent("access_type", "offline"); // enable if you later need refresh tokens from Google
    return OAuth2AuthorizationRequest.from(ar).additionalParameters(extra).build();
  }
}

