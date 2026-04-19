package com.taskengine.backend.service.impl;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.taskengine.backend.dto.*;
import com.taskengine.backend.entity.*;
import com.taskengine.backend.exception.BadRequestException;
import com.taskengine.backend.exception.ResourceNotFoundException;
import com.taskengine.backend.repository.TaskAuditLogRepository;
import com.taskengine.backend.repository.TaskRepository;
import com.taskengine.backend.repository.UserRepository;
import com.taskengine.backend.repository.spec.TaskSpecifications;
import com.taskengine.backend.security.TaskPermissionEvaluator;
import com.taskengine.backend.security.UserPrincipal;
import com.taskengine.backend.service.AuditLogService;
import com.taskengine.backend.service.TaskService;
import com.taskengine.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepository;
  private final TaskAuditLogRepository taskAuditLogRepository;
  private final AuditLogService auditLogService;
  private final UserRepository userRepository;
  private final SecurityUtils securityUtils;
  private final TaskPermissionEvaluator taskPermissionEvaluator;

  @Override
  @Transactional(readOnly = true)
  public TaskStatsResponse getTaskStats() {
    User user = securityUtils.getCurrentUser();
    Specification<Task> base =
        TaskSpecifications.forUserRole(
                user.getOrganization().getId(), user.getRole(), user.getId())
            .and(TaskSpecifications.notDeleted());
    long total = taskRepository.count(base);
    long todo =
        taskRepository.count(base.and(TaskSpecifications.optionalStatus(TaskStatus.TODO)));
    long inProgress =
        taskRepository.count(base.and(TaskSpecifications.optionalStatus(TaskStatus.IN_PROGRESS)));
    long inReview =
        taskRepository.count(base.and(TaskSpecifications.optionalStatus(TaskStatus.IN_REVIEW)));
    long done =
        taskRepository.count(base.and(TaskSpecifications.optionalStatus(TaskStatus.DONE)));
    return TaskStatsResponse.builder()
        .total(total)
        .todo(todo)
        .inProgress(inProgress)
        .inReview(inReview)
        .done(done)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public Page<TaskResponse> searchTasks(
      TaskStatus status,
      TaskPriority priority,
      UUID assignedTo,
      String search,
      Pageable pageable) {
    User user = securityUtils.getCurrentUser();
    UUID orgId = user.getOrganization().getId();
    String q = search == null || search.isBlank() ? null : search.trim();
    Specification<Task> spec =
        TaskSpecifications.withFetchUsers()
            .and(TaskSpecifications.forUserRole(orgId, user.getRole(), user.getId()))
            .and(TaskSpecifications.notDeleted())
            .and(TaskSpecifications.optionalStatus(status))
            .and(TaskSpecifications.optionalPriority(priority))
            .and(TaskSpecifications.optionalAssignedTo(assignedTo))
            .and(TaskSpecifications.search(q));
    return taskRepository.findAll(spec, pageable).map(this::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public TaskResponse getTask(UUID taskId) {
    User user = securityUtils.getCurrentUser();
    Task task =
        taskRepository
            .findByIdAndOrganizationIdWithUsers(taskId, user.getOrganization().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    taskPermissionEvaluator.assertCanRead(task, securityUtils.getCurrentPrincipal());
    return toDto(task);
  }

  @Override
  @Transactional
  public TaskResponse createTask(TaskRequest request) {
    User user = securityUtils.getCurrentUser();
    UserPrincipal principal = securityUtils.getCurrentPrincipal();
    if (request.getAssignedToId() != null) {
      taskPermissionEvaluator.assertCanAssign(principal);
    }
    Task task = new Task();
    task.setOrganization(user.getOrganization());
    task.setCreatedBy(user);
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStatus(request.getStatus());
    task.setPriority(request.getPriority());
    task.setDueDate(request.getDueDate());
    if (request.getAssignedToId() != null) {
      task.setAssignedTo(resolveAssignee(request.getAssignedToId(), user.getOrganization().getId()));
    }
    Task saved = taskRepository.save(task);
    scheduleAuditAfterCommit(
        saved.getId(),
        user.getId(),
        TaskAuditAction.CREATED,
        null,
        fullTaskSnapshot(saved));
    return toDto(saved);
  }

  @Override
  @Transactional
  public TaskResponse updateTask(UUID taskId, TaskRequest request) {
    User user = securityUtils.getCurrentUser();
    UserPrincipal principal = securityUtils.getCurrentPrincipal();
    Task task =
        taskRepository
            .findByIdAndOrganizationIdWithUsers(taskId, user.getOrganization().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    taskPermissionEvaluator.assertCanWrite(task, principal);
    if (task.isDeleted()) {
      throw new BadRequestException("Task is deleted");
    }
    Map<String, Object> before = fullTaskSnapshot(task);

    TaskStatus prevStatus = task.getStatus();
    UUID prevAssignee =
        task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;

    UUID newAssigneeId = request.getAssignedToId();
    boolean assigneeChanged =
        (prevAssignee == null && newAssigneeId != null)
            || (prevAssignee != null && !prevAssignee.equals(newAssigneeId));
    taskPermissionEvaluator.assertAssigneeChangeAllowed(principal, assigneeChanged);

    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStatus(request.getStatus());
    task.setPriority(request.getPriority());
    task.setDueDate(request.getDueDate());
    if (request.getAssignedToId() != null) {
      task.setAssignedTo(resolveAssignee(request.getAssignedToId(), user.getOrganization().getId()));
    } else {
      task.setAssignedTo(null);
    }

    Task saved = taskRepository.save(task);

    boolean statusChanged = prevStatus != request.getStatus();
    TaskAuditAction action = TaskAuditAction.UPDATED;
    if (statusChanged) {
      action = TaskAuditAction.STATUS_CHANGED;
    } else if (assigneeChanged) {
      action = TaskAuditAction.ASSIGNED;
    }

    TaskAuditAction finalAction = action;
    scheduleAuditAfterCommit(
        saved.getId(), user.getId(), finalAction, before, fullTaskSnapshot(saved));
    return toDto(saved);
  }

  @Override
  @Transactional
  public TaskResponse patchTaskStatus(UUID taskId, PatchTaskStatusRequest request) {
    User user = securityUtils.getCurrentUser();
    Task task =
        taskRepository
            .findByIdAndOrganizationIdWithUsers(taskId, user.getOrganization().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    taskPermissionEvaluator.assertCanWrite(task, securityUtils.getCurrentPrincipal());
    if (task.isDeleted()) {
      throw new BadRequestException("Task is deleted");
    }
    Map<String, Object> before = fullTaskSnapshot(task);
    TaskStatus prev = task.getStatus();
    if (prev == request.getStatus()) {
      return toDto(task);
    }
    task.setStatus(request.getStatus());
    Task saved = taskRepository.save(task);
    scheduleAuditAfterCommit(
        saved.getId(),
        user.getId(),
        TaskAuditAction.STATUS_CHANGED,
        before,
        fullTaskSnapshot(saved));
    return toDto(saved);
  }

  @Override
  @Transactional
  public void deleteTask(UUID taskId) {
    User user = securityUtils.getCurrentUser();
    Task task =
        taskRepository
            .findByIdAndOrganizationIdWithUsers(taskId, user.getOrganization().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    taskPermissionEvaluator.assertCanDelete(task, securityUtils.getCurrentPrincipal());
    if (task.isDeleted()) {
      return;
    }
    Map<String, Object> before = fullTaskSnapshot(task);
    task.setDeleted(true);
    task.setDeletedAt(Instant.now());
    Task saved = taskRepository.save(task);
    scheduleAuditAfterCommit(
        saved.getId(),
        user.getId(),
        TaskAuditAction.DELETED,
        before,
        fullTaskSnapshot(saved));
  }

  @Override
  @Transactional
  public TaskResponse restoreTask(UUID taskId) {
    User user = securityUtils.getCurrentUser();
    UserPrincipal principal = securityUtils.getCurrentPrincipal();
    Task task =
        taskRepository
            .findByIdAndOrganizationIdWithUsers(taskId, user.getOrganization().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    taskPermissionEvaluator.assertCanWrite(task, principal);
    if (!task.isDeleted()) {
      throw new BadRequestException("Task is not deleted");
    }
    Map<String, Object> before = fullTaskSnapshot(task);
    task.setDeleted(false);
    task.setDeletedAt(null);
    Task saved = taskRepository.save(task);
    scheduleAuditAfterCommit(
        saved.getId(),
        user.getId(),
        TaskAuditAction.UPDATED,
        before,
        fullTaskSnapshot(saved));
    return toDto(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public List<TaskHistoryEntryResponse> getTaskHistory(UUID taskId) {
    User user = securityUtils.getCurrentUser();
    Task task =
        taskRepository
            .findByIdAndOrganizationIdWithUsers(taskId, user.getOrganization().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    taskPermissionEvaluator.assertCanRead(task, securityUtils.getCurrentPrincipal());
    return taskAuditLogRepository
        .findByTaskIdAndOrgId(taskId, user.getOrganization().getId())
        .stream()
        .map(this::toHistoryDto)
        .toList();
  }

  private void scheduleAuditAfterCommit(
      UUID taskId,
      UUID actorId,
      TaskAuditAction action,
      Map<String, Object> oldValue,
      Map<String, Object> newValue) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(
          new TransactionSynchronization() {
            @Override
            public void afterCommit() {
              auditLogService.logAction(taskId, actorId, action, oldValue, newValue);
            }
          });
    } else {
      auditLogService.logAction(taskId, actorId, action, oldValue, newValue);
    }
  }

  private User resolveAssignee(UUID userId, UUID orgId) {
    return userRepository
        .findByIdAndOrganizationId(userId, orgId)
        .orElseThrow(() -> new ResourceNotFoundException("Assignee not found in organization"));
  }

  /** Full task state for JSONB audit columns (before/after mutations). */
  private Map<String, Object> fullTaskSnapshot(Task t) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", t.getId().toString());
    m.put("organizationId", t.getOrganization().getId().toString());
    m.put("title", t.getTitle());
    m.put("description", t.getDescription());
    m.put("status", t.getStatus().name());
    m.put("priority", t.getPriority().name());
    m.put("dueDate", t.getDueDate() != null ? t.getDueDate().toString() : null);
    m.put("createdById", t.getCreatedBy().getId().toString());
    m.put("createdByName", t.getCreatedBy().getFullName());
    m.put(
        "assignedToId",
        t.getAssignedTo() != null ? t.getAssignedTo().getId().toString() : null);
    m.put("assignedToName", t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : null);
    m.put("createdAt", t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
    m.put("updatedAt", t.getUpdatedAt() != null ? t.getUpdatedAt().toString() : null);
    m.put("deleted", t.isDeleted());
    m.put("deletedAt", t.getDeletedAt() != null ? t.getDeletedAt().toString() : null);
    return m;
  }

  private TaskResponse toDto(Task task) {
    User cb = task.getCreatedBy();
    User at = task.getAssignedTo();
    return TaskResponse.builder()
        .id(task.getId())
        .title(task.getTitle())
        .description(task.getDescription())
        .status(task.getStatus())
        .priority(task.getPriority())
        .createdBy(cb.getId())
        .createdByName(cb.getFullName())
        .createdByEmail(cb.getEmail())
        .createdByAvatarUrl(cb.getAvatarUrl())
        .assignedTo(at != null ? at.getId() : null)
        .assignedToName(at != null ? at.getFullName() : null)
        .assignedToEmail(at != null ? at.getEmail() : null)
        .assignedToAvatarUrl(at != null ? at.getAvatarUrl() : null)
        .organizationId(task.getOrganization().getId())
        .dueDate(task.getDueDate())
        .createdAt(task.getCreatedAt())
        .updatedAt(task.getUpdatedAt())
        .deleted(task.isDeleted())
        .deletedAt(task.getDeletedAt())
        .build();
  }

  private TaskHistoryEntryResponse toHistoryDto(TaskAuditLog log) {
    return TaskHistoryEntryResponse.builder()
        .id(log.getId())
        .action(log.getAction())
        .actorId(log.getActor().getId())
        .actorName(log.getActor().getFullName())
        .actorAvatarUrl(log.getActor().getAvatarUrl())
        .oldValue(log.getOldValue())
        .newValue(log.getNewValue())
        .timestamp(log.getTimestamp())
        .build();
  }
}
