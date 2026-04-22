package com.taskengine.backend.service.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskengine.backend.dto.MessageResponse;
import com.taskengine.backend.dto.SendMessageRequest;
import com.taskengine.backend.entity.Message;
import com.taskengine.backend.entity.User;
import com.taskengine.backend.exception.ResourceNotFoundException;
import com.taskengine.backend.repository.MessageRepository;
import com.taskengine.backend.repository.UserRepository;
import com.taskengine.backend.service.MessageService;
import com.taskengine.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final UserRepository userRepository;
  private final SecurityUtils securityUtils;

  @Override
  @Transactional
  public MessageResponse sendMessage(SendMessageRequest request) {
    User sender = securityUtils.getCurrentUser();
    Message message = new Message();
    message.setOrganization(sender.getOrganization());
    message.setSender(sender);
    message.setContent(request.getContent().trim());
    if (request.getReceiverId() != null) {
      User receiver =
          userRepository
              .findByIdAndOrganizationId(request.getReceiverId(), sender.getOrganization().getId())
              .orElseThrow(() -> new ResourceNotFoundException("Receiver not found in organization"));
      message.setReceiver(receiver);
    }
    return toDto(messageRepository.save(message));
  }

  @Override
  @Transactional(readOnly = true)
  public List<MessageResponse> getOrgMessages() {
    User user = securityUtils.getCurrentUser();
    return messageRepository.findOrgChannelMessages(user.getOrganization().getId()).stream()
        .limit(200)
        .map(this::toDto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<MessageResponse> getConversation(UUID userId) {
    User current = securityUtils.getCurrentUser();
    userRepository
        .findByIdAndOrganizationId(userId, current.getOrganization().getId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found in organization"));
    return messageRepository
        .findDirectMessages(current.getOrganization().getId(), current.getId(), userId)
        .stream()
        .limit(200)
        .map(this::toDto)
        .toList();
  }

  private MessageResponse toDto(Message m) {
    return MessageResponse.builder()
        .id(m.getId())
        .senderId(m.getSender().getId())
        .senderName(m.getSender().getFullName())
        .receiverId(m.getReceiver() != null ? m.getReceiver().getId() : null)
        .receiverName(m.getReceiver() != null ? m.getReceiver().getFullName() : null)
        .content(m.getContent())
        .createdAt(m.getCreatedAt())
        .build();
  }
}
