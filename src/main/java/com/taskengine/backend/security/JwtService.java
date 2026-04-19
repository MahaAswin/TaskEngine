package com.taskengine.backend.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.taskengine.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

  private final SecretKey secretKey;
  private final long accessExpirationMs;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.access-expiration-ms}") long accessExpirationMs) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessExpirationMs = accessExpirationMs;
  }

  public String generateAccessToken(User user) {
    UserPrincipal p = UserPrincipal.fromEntity(user);
    return generateAccessToken(p);
  }

  public String generateAccessToken(UserPrincipal principal) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(principal.getId().toString())
        .claim("email", principal.getEmail())
        .claim("orgId", principal.getOrgId().toString())
        .claim("role", principal.getRole().name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(accessExpirationMs)))
        .signWith(secretKey)
        .compact();
  }

  public String extractSubject(String token) {
    return parse(token).getSubject();
  }

  public UUID extractOrgId(String token) {
    String raw = parse(token).get("orgId", String.class);
    return raw != null ? UUID.fromString(raw) : null;
  }

  public boolean isAccessTokenValid(String token, UserPrincipal principal) {
    Claims claims = parse(token);
    return claims.getSubject().equals(principal.getId().toString())
        && claims.getExpiration().after(new Date());
  }

  private Claims parse(String token) {
    return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
  }
}
