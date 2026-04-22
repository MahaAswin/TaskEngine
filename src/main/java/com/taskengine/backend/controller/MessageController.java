package com.taskengine.backend.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.taskengine.backend.dto.MessageResponse;
import com.taskengine.backend.dto.SendMessageRequest;
import com.taskengine.backend.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/messages")
@Validated
@RequiredArgsConstructor
@Tag(name = "Messages")
public class MessageController {

  private final MessageService messageService;

  @PostMapping
  @Operation(summary = "Send org-wide or direct message")
  public MessageResponse sendMessage(@Valid @RequestBody SendMessageRequest request) {
    return messageService.sendMessage(request);
  }

  @GetMapping("/org")
  @Operation(summary = "Get organization-wide channel messages")
  public List<MessageResponse> orgMessages() {
    return messageService.getOrgMessages();
  }

  @GetMapping("/conversation/{userId}")
  @Operation(summary = "Get direct conversation with user")
  public List<MessageResponse> conversation(@PathVariable UUID userId) {
    return messageService.getConversation(userId);
  }
}
