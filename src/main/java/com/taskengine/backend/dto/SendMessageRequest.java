package com.taskengine.backend.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {
  private UUID receiverId;
  @NotBlank private String content;
}
