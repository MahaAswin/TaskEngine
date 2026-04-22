package com.taskengine.backend.util;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.taskengine.backend.entity.User;
import com.taskengine.backend.exception.ForbiddenException;
import com.taskengine.backend.repository.UserRepository;
import com.taskengine.backend.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class SecurityUtils {

  private final UserRepository userRepository;

  public UserPrincipal getCurrentPrincipal() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
      throw new IllegalStateException("No authenticated user");
    }
    return principal;
  }

  public UUID getCurrentOrgId() {
    return getCurrentPrincipal().getOrgId();
  }

  /** Loads the current user entity with organization (tenant-scoped). */
  public User getCurrentUser() {
    UserPrincipal p = getCurrentPrincipal();
    return userRepository
        .findByIdAndOrganizationId(p.getId(), p.getOrgId())
        .orElseGet(
            () -> {
              // Compatibility fallback for stale JWT orgId claims after migrations or tenant changes.
              log.warn(
                  "Principal orgId mismatch for userId={}, tokenOrgId={}. Falling back to user lookup by id.",
                  p.getId(),
                  p.getOrgId());
              return userRepository
                  .findByIdWithOrganization(p.getId())
                  .orElseThrow(() -> new ForbiddenException("Authenticated user no longer exists"));
            });
  }
}
