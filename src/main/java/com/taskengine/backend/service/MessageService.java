package com.taskengine.backend.service;

import java.util.List;
import java.util.UUID;
import com.taskengine.backend.dto.MessageResponse;
import com.taskengine.backend.dto.SendMessageRequest;

public interface MessageService {
  MessageResponse sendMessage(SendMessageRequest request);

  List<MessageResponse> getOrgMessages();

  List<MessageResponse> getConversation(UUID userId);
}
