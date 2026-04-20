package com.taskengine.backend.dto;

import java.time.Instant;
import java.util.UUID;
import com.taskengine.backend.entity.TeamMemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamMemberResponse {
  private final UUID userId;
  private final String fullName;
  private final String avatarUrl;
  private final TeamMemberRole role;
  private final Instant joinedAt;
}
