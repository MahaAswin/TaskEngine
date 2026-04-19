package com.taskengine.backend.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import com.taskengine.backend.entity.TaskAuditAction;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskHistoryEntryResponse {

  private final UUID id;
  private final TaskAuditAction action;
  private final UUID actorId;
  private final String actorName;
  private final String actorAvatarUrl;
  private final Map<String, Object> oldValue;
  private final Map<String, Object> newValue;
  private final Instant timestamp;
}
