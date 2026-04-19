package com.taskengine.backend.controller;

import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.taskengine.backend.dto.ChangePasswordRequest;
import com.taskengine.backend.dto.PatchUserRoleRequest;
import com.taskengine.backend.dto.UpdateProfileRequest;
import com.taskengine.backend.dto.UserMeResponse;
import com.taskengine.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@Validated
@RequiredArgsConstructor
@Tag(name = "Users")
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  @Operation(summary = "Current user profile")
  public UserMeResponse me() {
    return userService.getCurrentUserProfile();
  }

  @PatchMapping("/me/profile")
  @Operation(summary = "Update profile")
  public void updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
    userService.updateProfile(request);
  }

  @PatchMapping("/me/password")
  @Operation(summary = "Change password")
  public void changePassword(@Valid @RequestBody ChangePasswordRequest request) {
    userService.changePassword(request);
  }

  @PatchMapping("/{id}/role")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Change member role (admin only)")
  public void patchRole(
      @PathVariable UUID id, @Valid @RequestBody PatchUserRoleRequest request) {
    userService.patchUserRole(id, request);
  }
}
