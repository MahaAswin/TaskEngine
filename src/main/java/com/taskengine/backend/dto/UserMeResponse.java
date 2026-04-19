package com.taskengine.backend.dto;

import java.util.UUID;
import com.taskengine.backend.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMeResponse {

  private final UUID id;
  private final String email;
  private final String fullName;
  private final UserRole role;
  private final String avatarUrl;
  private final UUID organizationId;
  private final String organizationName;
  private final String organizationSlug;
}
