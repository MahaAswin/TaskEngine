package com.taskengine.backend.dto;

import java.time.LocalDate;
import java.util.UUID;
import com.taskengine.backend.entity.TaskPriority;
import com.taskengine.backend.entity.TaskScope;
import com.taskengine.backend.entity.TaskStatus;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskRequest {

  @NotBlank private String title;

  @Size(max = 500)
  private String description;

  @NotNull private TaskStatus status;

  @NotNull private TaskPriority priority;

  @NotNull private TaskScope scope;

  private UUID teamId;

  @JsonAlias("assignedTo")
  private UUID assignedToId;

  private LocalDate dueDate;
}
