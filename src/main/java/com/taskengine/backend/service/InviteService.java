package com.taskengine.backend.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskengine.backend.dto.InviteRequest;
import com.taskengine.backend.entity.OrgInvite;
import com.taskengine.backend.entity.Organization;
import com.taskengine.backend.entity.User;
import com.taskengine.backend.exception.BadRequestException;
import com.taskengine.backend.repository.OrgInviteRepository;
import com.taskengine.backend.repository.UserRepository;
import com.taskengine.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteService {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final OrgInviteRepository orgInviteRepository;
  private final UserRepository userRepository;
  private final SecurityUtils securityUtils;

  @Value("${app.frontend.base-url:http://localhost:5173}")
  private String frontendBaseUrl;

  @Transactional
  public void sendInvite(InviteRequest request) {
    User admin = securityUtils.getCurrentUser();
    if (admin.getOrganization() == null) {
      throw new BadRequestException("No organization");
    }
    Organization org = admin.getOrganization();
    String email = request.getEmail().trim().toLowerCase();
    if (userRepository.existsByEmail(email)) {
      throw new BadRequestException("User with this email already exists");
    }
    byte[] raw = new byte[32];
    RANDOM.nextBytes(raw);
    String plain = HexFormat.of().formatHex(raw);
    OrgInvite inv = new OrgInvite();
    inv.setOrganization(org);
    inv.setEmail(email);
    inv.setRole(request.getRole());
    inv.setTokenHash(sha256Hex(plain));
    inv.setExpiresAt(Instant.now().plusSeconds(7L * 24 * 60 * 60));
    orgInviteRepository.save(inv);
    String link = frontendBaseUrl + "/register?inviteToken=" + plain;
    log.info("Invite email to {} (org {}): {}", email, org.getName(), link);
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
