package com.taskengine.backend.dto;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTeamRequest {
  @NotBlank private String name;
  private String description;
  private List<UUID> memberIds;
}
