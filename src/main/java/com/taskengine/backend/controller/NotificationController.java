package com.taskengine.backend.controller;

import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.taskengine.backend.security.UserPrincipal;
import com.taskengine.backend.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@Validated
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  @Operation(summary = "SSE stream for organization notifications")
  public SseEmitter stream(Authentication authentication) {
    UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
    UUID orgId = principal.getOrgId();
    UUID userId = principal.getId();
    return notificationService.subscribe(orgId, userId);
  }
}
