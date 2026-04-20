package com.taskengine.backend.dto;

import java.time.Instant;
import java.util.UUID;
import com.taskengine.backend.entity.TeamMemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamResponse {
  private final UUID id;
  private final String name;
  private final String description;
  private final long memberCount;
  private final UUID createdBy;
  private final Instant createdAt;
  private final TeamMemberRole myRole;
}
