package com.taskengine.backend.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MessageResponse {
  private final UUID id;
  private final UUID senderId;
  private final String senderName;
  private final UUID receiverId;
  private final String receiverName;
  private final String content;
  private final Instant createdAt;
}
