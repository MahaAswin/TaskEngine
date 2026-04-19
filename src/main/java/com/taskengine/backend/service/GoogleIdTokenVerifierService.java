package com.taskengine.backend.service;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.taskengine.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GoogleIdTokenVerifierService {

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String clientId;

  public GooglePayload verify(String idTokenString) {
    if (idTokenString == null || idTokenString.isBlank()) {
      throw new BadRequestException("idToken is required");
    }
    try {
      GoogleIdTokenVerifier verifier =
          new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
              .setAudience(Collections.singletonList(clientId))
              .build();
      GoogleIdToken idToken = verifier.verify(idTokenString);
      if (idToken == null) {
        throw new BadRequestException("Invalid Google token");
      }
      return new GooglePayload(
          idToken.getPayload().getSubject(),
          (String) idToken.getPayload().get("email"),
          (String) idToken.getPayload().get("name"),
          (String) idToken.getPayload().get("picture"));
    } catch (Exception e) {
      throw new BadRequestException("Could not verify Google token");
    }
  }

  public record GooglePayload(String sub, String email, String name, String picture) {}
}
