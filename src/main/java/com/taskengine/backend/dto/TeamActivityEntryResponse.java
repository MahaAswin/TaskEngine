package com.taskengine.backend.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamActivityEntryResponse {
  private final UUID id;
  private final String source;
  private final String action;
  private final UUID taskId;
  private final String taskTitle;
  private final UUID actorId;
  private final String actorName;
  private final UUID affectedUserId;
  private final String affectedUserName;
  private final Instant timestamp;
}
