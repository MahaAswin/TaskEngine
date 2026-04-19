package com.taskengine.backend.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.taskengine.backend.entity.TaskAuditAction;
import com.taskengine.backend.entity.TaskAuditLog;
import com.taskengine.backend.repository.TaskAuditLogRepository;
import com.taskengine.backend.repository.TaskRepository;
import com.taskengine.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * Persists task audit entries asynchronously so API latency is not blocked by audit writes.
 * JSON snapshots ({@code oldValue} / {@code newValue}) map to JSONB columns.
 */
@Service
@RequiredArgsConstructor
public class AuditLogService {

  private final TaskAuditLogRepository taskAuditLogRepository;
  private final TaskRepository taskRepository;
  private final UserRepository userRepository;

  @Async
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void logAction(
      UUID taskId,
      UUID actorId,
      TaskAuditAction action,
      Map<String, Object> oldValue,
      Map<String, Object> newValue) {
    TaskAuditLog log = new TaskAuditLog();
    log.setTask(taskRepository.getReferenceById(taskId));
    log.setActor(userRepository.getReferenceById(actorId));
    log.setAction(action);
    log.setOldValue(oldValue);
    log.setNewValue(newValue);
    log.setTimestamp(Instant.now());
    taskAuditLogRepository.save(log);
  }
}
