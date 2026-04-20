package com.taskengine.backend.dto;

import com.taskengine.backend.entity.TeamMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTeamMemberRoleRequest {
  @NotNull private TeamMemberRole role;
}
