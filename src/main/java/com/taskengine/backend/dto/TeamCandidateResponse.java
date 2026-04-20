package com.taskengine.backend.dto;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TeamCandidateResponse {
  private final UUID id;
  private final String fullName;
  private final String email;
}
