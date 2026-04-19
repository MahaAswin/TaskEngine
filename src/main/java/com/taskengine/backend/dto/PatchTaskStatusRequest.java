package com.taskengine.backend.dto;

import com.taskengine.backend.entity.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PatchTaskStatusRequest {

  @NotNull private TaskStatus status;
}
