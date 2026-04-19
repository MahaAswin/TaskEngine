package com.taskengine.backend.dto;

import java.time.LocalDate;
import java.util.UUID;
import com.taskengine.backend.entity.TaskPriority;
import com.taskengine.backend.entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {

  @NotBlank private String title;

  private String description;

  @NotNull private TaskStatus status;

  @NotNull private TaskPriority priority;

  private UUID assignedToId;

  private LocalDate dueDate;
}
