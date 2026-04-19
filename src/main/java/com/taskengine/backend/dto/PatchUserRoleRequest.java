package com.taskengine.backend.dto;

import com.taskengine.backend.entity.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchUserRoleRequest {

  @NotNull private UserRole role;
}
