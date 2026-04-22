package com.taskengine.backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskengine.backend.dto.*;
import com.taskengine.backend.entity.*;
import com.taskengine.backend.exception.BadRequestException;
import com.taskengine.backend.repository.OrganizationRepository;
import com.taskengine.backend.repository.UserRepository;
import com.taskengine.backend.security.JwtService;
import com.taskengine.backend.service.AuthService;
import com.taskengine.backend.service.GoogleIdTokenVerifierService;
import com.taskengine.backend.service.GoogleIdTokenVerifierService.GooglePayload;
import com.taskengine.backend.service.RefreshTokenService;
import com.taskengine.backend.util.Slugify;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final RefreshTokenService refreshTokenService;
  private final AuthenticationManager authenticationManager;
  private final GoogleIdTokenVerifierService googleIdTokenVerifierService;

  @Value("${app.jwt.access-expiration-ms}")
  private long accessExpirationMs;

  @Override
  @Transactional
  public AuthResponse register(RegisterRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new BadRequestException("Email already exists");
    }
    Organization org = new Organization();
    org.setName(request.getOrganizationName());
    org.setSlug(uniqueSlug(Slugify.baseSlug(request.getOrganizationName())));
    org.setPlan(OrganizationPlan.FREE);
    organizationRepository.save(org);

    User user = new User();
    user.setOrganization(org);
    user.setEmail(request.getEmail().trim().toLowerCase());
    user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    user.setFullName(request.getFullName());
    user.setRole(UserRole.ADMIN);
    user.setActive(true);
    userRepository.save(user);

    return issueTokens(user.getId());
  }

  @Override
  @Transactional
  public AuthResponse login(AuthRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail().trim().toLowerCase(), request.getPassword()));
    User user =
        userRepository
            .findByEmailWithOrganization(request.getEmail().trim().toLowerCase())
            .orElseThrow(() -> new BadRequestException("Invalid credentials"));
    if (!user.isActive()) {
      throw new BadRequestException("Account disabled");
    }
    return issueTokens(user.getId());
  }

  @Override
  @Transactional
  public AuthResponse refresh(RefreshRequest request) {
    return refreshWithToken(request.getRefreshToken());
  }

  @Override
  @Transactional
  public AuthResponse refreshWithToken(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new BadRequestException("Refresh token required");
    }
    User user = refreshTokenService.validateAndConsume(refreshToken);
    if (!user.isActive()) {
      throw new BadRequestException("Account disabled");
    }
    return issueTokens(user.getId());
  }

  @Override
  @Transactional
  public void logout(LogoutRequest request) {
    logoutWithToken(request.getRefreshToken());
  }

  @Override
  @Transactional
  public void logoutWithToken(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }
    refreshTokenService.revoke(refreshToken);
  }

  @Override
  @Transactional
  public AuthResponse oauthCallback(OAuth2CallbackRequest request) {
    GooglePayload payload = googleIdTokenVerifierService.verify(request.getIdToken());
    return oauthLogin(payload.sub(), payload.email(), payload.name(), payload.picture());
  }

  @Override
  @Transactional
  public AuthResponse oauthLogin(String googleSub, String email, String fullName, String avatarUrl) {
    if (email == null || email.isBlank()) {
      throw new BadRequestException("Google account has no email");
    }
    if (googleSub == null || googleSub.isBlank()) {
      throw new BadRequestException("Google account subject is missing");
    }
    String normalizedEmail = email.trim().toLowerCase();
    log.info("OAuth login → email={}, sub={}", normalizedEmail, googleSub);

    User user = userRepository.findByGoogleSub(googleSub).orElse(null);
    Organization targetOrg = resolveOrganizationForEmail(normalizedEmail);

    if (user == null) {
      user = new User();
      user.setOrganization(targetOrg);
      user.setEmail(normalizedEmail);
      user.setGoogleSub(googleSub);
      user.setFullName(fullName != null && !fullName.isBlank() ? fullName : normalizedEmail);
      user.setAvatarUrl(avatarUrl);
      user.setRole(UserRole.MEMBER);
      user.setActive(true);
      userRepository.save(user);
    } else {
      if (!user.getOrganization().getId().equals(targetOrg.getId())) {
        user.setOrganization(targetOrg);
        userRepository.save(user);
      }
    }
    
    log.info("User {} → orgId={}", user.getEmail(), user.getOrganization().getId());

    return issueTokens(user.getId());
  }

  private AuthResponse issueTokens(java.util.UUID userId) {
    User user =
        userRepository
            .findByIdWithOrganization(userId)
            .orElseThrow(() -> new BadRequestException("User not found"));
    String access = jwtService.generateAccessToken(user);
    String refresh = refreshTokenService.createRefreshToken(user);
    return new AuthResponse(access, refresh, accessExpirationMs / 1000);
  }

  private String uniqueSlug(String base) {
    String slug = base;
    int i = 0;
    while (organizationRepository.existsBySlug(slug)) {
      slug = base + "-" + (++i);
    }
    return slug;
  }

  private Organization resolveOrganizationForEmail(String email) {
    String domain = email.substring(email.indexOf('@') + 1).toLowerCase().trim();

    return organizationRepository
        .findBySlug(domain)
        .orElseGet(
            () -> {
              Organization org = new Organization();
              org.setName(domain);
              org.setSlug(domain);
              org.setPlan(OrganizationPlan.FREE);
              return organizationRepository.save(org);
            });
  }
}
