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

@Service
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
    if (payload.email() == null || payload.email().isBlank()) {
      throw new BadRequestException("Google account has no email");
    }
    String email = payload.email().trim().toLowerCase();

    var existingBySub = userRepository.findByGoogleSubWithOrganization(payload.sub());
    if (existingBySub.isPresent()) {
      User u = existingBySub.get();
      if (!u.isActive()) {
        throw new BadRequestException("Account disabled");
      }
      return issueTokens(u.getId());
    }

    var existingByEmail = userRepository.findByEmailWithOrganization(email);
    if (existingByEmail.isPresent()) {
      User u = existingByEmail.get();
      u.setGoogleSub(payload.sub());
      if (payload.picture() != null) {
        u.setAvatarUrl(payload.picture());
      }
      userRepository.save(u);
      return issueTokens(u.getId());
    }

    Organization org = new Organization();
    String orgName = payload.name() != null ? payload.name() + "'s workspace" : "Workspace";
    org.setName(orgName);
    org.setSlug(uniqueSlug(Slugify.baseSlug(orgName)));
    org.setPlan(OrganizationPlan.FREE);
    organizationRepository.save(org);

    User user = new User();
    user.setOrganization(org);
    user.setEmail(email);
    user.setGoogleSub(payload.sub());
    user.setFullName(payload.name() != null ? payload.name() : email);
    user.setAvatarUrl(payload.picture());
    user.setRole(UserRole.MEMBER);
    user.setActive(true);
    userRepository.save(user);

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
}
