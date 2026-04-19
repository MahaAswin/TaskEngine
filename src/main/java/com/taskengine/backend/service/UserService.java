package com.taskengine.backend.service;

import java.util.UUID;
import com.taskengine.backend.dto.*;

public interface UserService {

  UserMeResponse getCurrentUserProfile();

  void updateProfile(UpdateProfileRequest request);

  void changePassword(ChangePasswordRequest request);

  void patchUserRole(UUID userId, PatchUserRoleRequest request);
}
