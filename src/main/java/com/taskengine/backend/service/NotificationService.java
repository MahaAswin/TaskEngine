package com.taskengine.backend.service;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskengine.backend.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

  private final ObjectMapper objectMapper;
  private final ConcurrentHashMap<UUID, CopyOnWriteArrayList<SseEmitter>> emittersByOrgId =
      new ConcurrentHashMap<>();

  public SseEmitter subscribe(UUID orgId, UUID userId) {
    SseEmitter emitter = new SseEmitter(300_000L);
    CopyOnWriteArrayList<SseEmitter> emitters =
        emittersByOrgId.computeIfAbsent(orgId, ignored -> new CopyOnWriteArrayList<>());
    emitters.add(emitter);

    Runnable cleanup =
        () -> {
          CopyOnWriteArrayList<SseEmitter> list = emittersByOrgId.get(orgId);
          if (list == null) {
            return;
          }
          list.remove(emitter);
          if (list.isEmpty()) {
            emittersByOrgId.remove(orgId, list);
          }
        };

    emitter.onCompletion(cleanup);
    emitter.onTimeout(cleanup);
    emitter.onError(ex -> cleanup.run());

    try {
      NotificationEvent connected =
          new NotificationEvent(
              "CONNECTED",
              "SSE connected",
              Map.of("orgId", orgId.toString(), "userId", userId.toString()),
              Instant.now());
      emitter.send(
          SseEmitter.event()
              .name("connected")
              .data(objectMapper.writeValueAsString(connected), MediaType.APPLICATION_JSON));
    } catch (IOException ex) {
      cleanup.run();
      emitter.completeWithError(ex);
    }

    return emitter;
  }

  public void pushToOrg(UUID orgId, String eventType, Object payload) {
    CopyOnWriteArrayList<SseEmitter> emitters = emittersByOrgId.get(orgId);
    if (emitters == null || emitters.isEmpty()) {
      return;
    }

    NotificationEvent event =
        new NotificationEvent(eventType, defaultMessage(eventType), payload, Instant.now());
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(
            SseEmitter.event()
                .name(eventType)
                .data(objectMapper.writeValueAsString(event), MediaType.APPLICATION_JSON));
      } catch (Exception ex) {
        emitters.remove(emitter);
      }
    }

    if (emitters.isEmpty()) {
      emittersByOrgId.remove(orgId, emitters);
    }
  }

  private static String defaultMessage(String eventType) {
    return switch (eventType) {
      case "TASK_CREATED" -> "A new task was created";
      case "TASK_UPDATED" -> "A task was updated";
      case "TASK_DELETED" -> "A task was deleted";
      case "TASK_STATUS_CHANGED" -> "A task status was changed";
      default -> "Notification";
    };
  }
}
