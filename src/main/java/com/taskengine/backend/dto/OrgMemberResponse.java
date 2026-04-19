package com.taskengine.backend.dto;

import java.util.UUID;
import com.taskengine.backend.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrgMemberResponse {

  private final UUID id;
  private final String fullName;
  private final String email;
  private final UserRole role;
  private final long taskCount;
}
