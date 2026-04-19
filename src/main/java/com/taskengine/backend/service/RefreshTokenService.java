package com.taskengine.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskengine.backend.entity.RefreshToken;
import com.taskengine.backend.entity.User;
import com.taskengine.backend.exception.BadRequestException;
import com.taskengine.backend.repository.RefreshTokenRepository;
import com.taskengine.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;

  @Value("${app.jwt.refresh-expiration-ms}")
  private long refreshExpirationMs;

  @Transactional
  public String createRefreshToken(User user) {
    byte[] bytes = new byte[32];
    RANDOM.nextBytes(bytes);
    String plain = HexFormat.of().formatHex(bytes);
    RefreshToken entity = new RefreshToken();
    entity.setUser(user);
    entity.setTokenHash(sha256Hex(plain));
    entity.setExpiresAt(Instant.now().plusMillis(refreshExpirationMs));
    entity.setRevoked(false);
    refreshTokenRepository.save(entity);
    return plain;
  }

  @Transactional
  public User validateAndConsume(String plainRefreshToken) {
    if (plainRefreshToken == null || plainRefreshToken.isBlank()) {
      throw new BadRequestException("Refresh token required");
    }
    String hash = sha256Hex(plainRefreshToken);
    RefreshToken stored =
        refreshTokenRepository
            .findByTokenHash(hash)
            .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
    if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
      throw new BadRequestException("Refresh token expired or revoked");
    }
    var userId = stored.getUser().getId();
    stored.setRevoked(true);
    refreshTokenRepository.save(stored);
    return userRepository
        .findByIdWithOrganization(userId)
        .orElseThrow(() -> new BadRequestException("User not found"));
  }

  @Transactional
  public void revoke(String plainRefreshToken) {
    if (plainRefreshToken == null || plainRefreshToken.isBlank()) {
      return;
    }
    String hash = sha256Hex(plainRefreshToken);
    refreshTokenRepository
        .findByTokenHash(hash)
        .ifPresent(
            t -> {
              t.setRevoked(true);
              refreshTokenRepository.save(t);
            });
  }

  private static String sha256Hex(String raw) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(digest.digest(raw.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
