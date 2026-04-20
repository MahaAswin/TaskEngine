package com.taskengine.backend.service;

import java.util.UUID;
import com.taskengine.backend.dto.*;

import java.util.List;

public interface UserService {

  UserMeResponse getCurrentUserProfile();

  List<UserMeResponse> listOrganizationUsers();

  void updateProfile(UpdateProfileRequest request);

  void changePassword(ChangePasswordRequest request);

  void patchUserRole(UUID userId, PatchUserRoleRequest request);
}
