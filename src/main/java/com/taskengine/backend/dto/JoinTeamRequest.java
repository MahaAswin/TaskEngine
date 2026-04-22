package com.taskengine.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JoinTeamRequest {
  @NotBlank private String inviteCode;
}
