package com.taskengine.backend.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import com.taskengine.backend.entity.TaskPriority;
import com.taskengine.backend.entity.TaskScope;
import com.taskengine.backend.entity.TaskStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaskResponse {

  private final UUID id;
  private final String title;
  private final String description;
  private final TaskStatus status;
  private final TaskPriority priority;
  private final TaskScope scope;
  private final UUID teamId;
  private final UUID createdBy;
  private final String createdByName;
  private final String createdByEmail;
  private final String createdByAvatarUrl;
  private final UUID assignedTo;
  private final String assignedToName;
  private final String assignedToEmail;
  private final String assignedToAvatarUrl;
  private final UUID organizationId;
  private final LocalDate dueDate;
  private final Instant createdAt;
  private final Instant updatedAt;
  private final boolean deleted;
  private final Instant deletedAt;
}
