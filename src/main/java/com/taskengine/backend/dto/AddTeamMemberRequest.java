package com.taskengine.backend.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddTeamMemberRequest {
  @NotNull private UUID userId;
}
