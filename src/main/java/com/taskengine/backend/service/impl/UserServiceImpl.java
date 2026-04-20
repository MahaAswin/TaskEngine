package com.taskengine.backend.service.impl;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskengine.backend.dto.*;
import com.taskengine.backend.entity.User;
import com.taskengine.backend.entity.UserRole;
import com.taskengine.backend.exception.BadRequestException;
import com.taskengine.backend.exception.PermissionDeniedException;
import com.taskengine.backend.exception.ResourceNotFoundException;
import com.taskengine.backend.repository.UserRepository;
import com.taskengine.backend.service.UserService;
import com.taskengine.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final SecurityUtils securityUtils;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public UserMeResponse getCurrentUserProfile() {
    User u = securityUtils.getCurrentUser();
    var o = u.getOrganization();
    return UserMeResponse.builder()
        .id(u.getId())
        .email(u.getEmail())
        .fullName(u.getFullName())
        .role(u.getRole())
        .avatarUrl(u.getAvatarUrl())
        .organizationId(o.getId())
        .organizationName(o.getName())
        .organizationSlug(o.getSlug())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserMeResponse> listOrganizationUsers() {
    User u = securityUtils.getCurrentUser();
    var org = u.getOrganization();
    return userRepository.findByOrganization_IdOrderByFullNameAsc(org.getId()).stream()
        .map(user -> UserMeResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole())
            .avatarUrl(user.getAvatarUrl())
            .organizationId(org.getId())
            .organizationName(org.getName())
            .organizationSlug(org.getSlug())
            .build())
        .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public void updateProfile(UpdateProfileRequest request) {
    User u = securityUtils.getCurrentUser();
    u.setFullName(request.getFullName());
    u.setAvatarUrl(request.getAvatarUrl());
    userRepository.save(u);
  }

  @Override
  @Transactional
  public void changePassword(ChangePasswordRequest request) {
    User u = securityUtils.getCurrentUser();
    if (u.getPasswordHash() == null) {
      throw new BadRequestException("Password login is not enabled for this account");
    }
    if (!passwordEncoder.matches(request.getCurrentPassword(), u.getPasswordHash())) {
      throw new BadRequestException("Current password is incorrect");
    }
    u.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    userRepository.save(u);
  }

  @Override
  @Transactional
  public void patchUserRole(UUID userId, PatchUserRoleRequest request) {
    User admin = securityUtils.getCurrentUser();
    if (admin.getRole() != UserRole.ADMIN) {
      throw new PermissionDeniedException("Only administrators can change roles");
    }
    User target =
        userRepository
            .findByIdAndOrganizationId(userId, admin.getOrganization().getId())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (target.getId().equals(admin.getId())) {
      throw new BadRequestException("You cannot change your own role here");
    }
    target.setRole(request.getRole());
    userRepository.save(target);
  }
}
