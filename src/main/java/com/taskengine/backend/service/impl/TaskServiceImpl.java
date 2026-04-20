package com.taskengine.backend.service.impl;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.taskengine.backend.dto.*;
import com.taskengine.backend.entity.*;
import com.taskengine.backend.exception.BadRequestException;
import com.taskengine.backend.exception.PermissionDeniedException;
import com.taskengine.backend.exception.ResourceNotFoundException;
import com.taskengine.backend.repository.TaskAuditLogRepository;
import com.taskengine.backend.repository.TaskRepository;
import com.taskengine.backend.repository.TeamMemberRepository;
import com.taskengine.backend.repository.TeamRepository;
import com.taskengine.backend.repository.UserRepository;
import com.taskengine.backend.service.AuditLogService;
import com.taskengine.backend.service.NotificationService;
import com.taskengine.backend.service.TaskService;
import com.taskengine.backend.service.TaskVisibilityService;
import com.taskengine.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

  private final TaskRepository taskRepository;
  private final TaskAuditLogRepository taskAuditLogRepository;
  private final AuditLogService auditLogService;
  private final UserRepository userRepository;
  private final TeamRepository teamRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final SecurityUtils securityUtils;
  private final TaskVisibilityService taskVisibilityService;
  private final NotificationService notificationService;
  private static final TaskScope GLOBAL_SCOPE = TaskScope.GLOBAL;
  private static final TaskScope TEAM_SCOPE = TaskScope.TEAM;
  private static final TaskScope PRIVATE_SCOPE = TaskScope.PRIVATE;

  @Override
  @Transactional(readOnly = true)
  public TaskStatsResponse getTaskStats() {
    User user = securityUtils.getCurrentUser();
    UUID orgId = user.getOrganization().getId();
    UUID userId = user.getId();
    boolean isAdmin = user.getRole() == UserRole.ADMIN;
    long total =
        taskRepository
            .findVisibleTasksNoStatusNoSearch(
                orgId,
                userId,
                isAdmin,
                GLOBAL_SCOPE,
                TEAM_SCOPE,
                PRIVATE_SCOPE,
                null,
                Pageable.unpaged())
            .getTotalElements();
    long todo =
        taskRepository
            .findVisibleTasksNoSearch(
                orgId,
                userId,
                isAdmin,
                GLOBAL_SCOPE,
                TEAM_SCOPE,
                PRIVATE_SCOPE,
                TaskStatus.TODO,
                null,
                Pageable.unpaged())
            .getTotalElements();
    long inProgress =
        taskRepository
            .findVisibleTasksNoSearch(
                orgId,
                userId,
                isAdmin,
                GLOBAL_SCOPE,
                TEAM_SCOPE,
                PRIVATE_SCOPE,
                TaskStatus.IN_PROGRESS,
                null,
                Pageable.unpaged())
            .getTotalElements();
    long inReview =
        taskRepository
            .findVisibleTasksNoSearch(
                orgId,
                userId,
                isAdmin,
                GLOBAL_SCOPE,
                TEAM_SCOPE,
                PRIVATE_SCOPE,
                TaskStatus.IN_REVIEW,
                null,
                Pageable.unpaged())
            .getTotalElements();
    long done =
        taskRepository
            .findVisibleTasksNoSearch(
                orgId,
                userId,
                isAdmin,
                GLOBAL_SCOPE,
                TEAM_SCOPE,
                PRIVATE_SCOPE,
                TaskStatus.DONE,
                null,
                Pageable.unpaged())
            .getTotalElements();
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
      UUID teamId,
      String search,
      Pageable pageable) {
    User user = securityUtils.getCurrentUser();
    String q = search == null || search.isBlank() ? null : search.trim();
    boolean isAdmin = user.getRole() == UserRole.ADMIN;
    UUID orgId = user.getOrganization().getId();
    UUID userId = user.getId();
    // Visibility is enforced in repository queries (org + scope + creator/assignee/team rules)
    // so pagination/sorting remain DB-backed and consistent.
    if (status == null && q == null) {
      return taskRepository
          .findVisibleTasksNoStatusNoSearch(
              orgId,
              userId,
              isAdmin,
              GLOBAL_SCOPE,
              TEAM_SCOPE,
              PRIVATE_SCOPE,
              teamId,
              pageable)
          .map(this::toDto);
    }
    if (status == null) {
      return taskRepository
          .findVisibleTasksNoStatus(
              orgId,
              userId,
              isAdmin,
              GLOBAL_SCOPE,
              TEAM_SCOPE,
              PRIVATE_SCOPE,
              teamId,
              q,
              pageable)
          .map(this::toDto);
    }
    if (q == null) {
      return taskRepository
          .findVisibleTasksNoSearch(
              orgId,
              userId,
              isAdmin,
              GLOBAL_SCOPE,
              TEAM_SCOPE,
              PRIVATE_SCOPE,
              status,
              teamId,
              pageable)
          .map(this::toDto);
    }
    return taskRepository
        .findVisibleTasks(
            orgId,
            userId,
            isAdmin,
            GLOBAL_SCOPE,
            TEAM_SCOPE,
            PRIVATE_SCOPE,
            status,
            teamId,
            q,
            pageable)
        .map(this::toDto);
  }

  @Override
  @Transactional(readOnly = true)
  public TaskResponse getTask(UUID taskId) {
    User user = securityUtils.getCurrentUser();
    Task task =
        taskRepository
            .findByIdAndOrganizationIdWithUsers(taskId, user.getOrganization().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    assertCanView(task, user);
    return toDto(task);
  }

  @Override
  @Transactional
  public TaskResponse createTask(TaskRequest request) {
    User user = securityUtils.getCurrentUser();
    Task task = new Task();
    task.setOrganization(user.getOrganization());
    task.setCreatedBy(user);
    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStatus(request.getStatus());
    task.setPriority(request.getPriority());
    task.setDueDate(request.getDueDate());
    applyScopeRules(task, request, user);
    if (request.getAssignedToId() != null) {
      User assignee = resolveAssignee(request.getAssignedToId(), user.getOrganization().getId());
      validateAssigneeForScope(task, assignee);
      task.setAssignedTo(assignee);
    }
    Task saved = taskRepository.save(task);
    scheduleAuditAfterCommit(
        saved.getId(),
        user.getId(),
        TaskAuditAction.CREATED,
        null,
        fullTaskSnapshot(saved));
    notificationService.pushToOrg(
        user.getOrganization().getId(),
        "TASK_CREATED",
        Map.of(
            "taskId", saved.getId().toString(),
            "title", saved.getTitle(),
            "createdBy", user.getFullName()));
    return toDto(saved);
  }

  @Override
  @Transactional
  public TaskResponse updateTask(UUID taskId, TaskRequest request) {
    User user = securityUtils.getCurrentUser();
    Task task =
        taskRepository
            .findByIdAndOrganizationIdWithUsers(taskId, user.getOrganization().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    assertCanMutate(task, user);
    if (task.isDeleted()) {
      throw new BadRequestException("Task is deleted");
    }
    Map<String, Object> before = fullTaskSnapshot(task);

    TaskStatus prevStatus = task.getStatus();
    TaskScope prevScope = task.getScope();
    UUID prevTeamId = task.getTeam() != null ? task.getTeam().getId() : null;
    UUID prevAssignee =
        task.getAssignedTo() != null ? task.getAssignedTo().getId() : null;

    task.setTitle(request.getTitle());
    task.setDescription(request.getDescription());
    task.setStatus(request.getStatus());
    task.setPriority(request.getPriority());
    task.setDueDate(request.getDueDate());
    applyScopeRules(task, request, user);
    if (request.getAssignedToId() != null) {
      User assignee = resolveAssignee(request.getAssignedToId(), user.getOrganization().getId());
      validateAssigneeForScope(task, assignee);
      task.setAssignedTo(assignee);
    } else {
      task.setAssignedTo(null);
    }

    UUID newAssigneeId = request.getAssignedToId();
    boolean assigneeChanged =
        (prevAssignee == null && newAssigneeId != null)
            || (prevAssignee != null && !prevAssignee.equals(newAssigneeId));

    Task saved = taskRepository.save(task);
    UUID newTeamId = saved.getTeam() != null ? saved.getTeam().getId() : null;
    boolean scopeChanged = prevScope != saved.getScope();
    boolean teamChanged =
        (prevTeamId == null && newTeamId != null)
            || (prevTeamId != null && !prevTeamId.equals(newTeamId));

    boolean statusChanged = prevStatus != request.getStatus();
    TaskAuditAction action = TaskAuditAction.UPDATED;
    if (scopeChanged) {
      action = TaskAuditAction.SCOPE_CHANGED;
    } else if (teamChanged) {
      action = TaskAuditAction.TEAM_CHANGED;
    } else if (statusChanged) {
      action = TaskAuditAction.STATUS_CHANGED;
    } else if (assigneeChanged) {
      action = TaskAuditAction.ASSIGNED;
    }

    TaskAuditAction finalAction = action;
    scheduleAuditAfterCommit(
        saved.getId(), user.getId(), finalAction, before, fullTaskSnapshot(saved));
    notificationService.pushToOrg(
        user.getOrganization().getId(),
        "TASK_UPDATED",
        Map.of("taskId", saved.getId().toString(), "title", saved.getTitle()));
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
    assertCanMutate(task, user);
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
    notificationService.pushToOrg(
        user.getOrganization().getId(),
        "TASK_STATUS_CHANGED",
        Map.of("taskId", saved.getId().toString(), "newStatus", saved.getStatus().name()));
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
    assertCanMutate(task, user);
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
    notificationService.pushToOrg(
        user.getOrganization().getId(),
        "TASK_DELETED",
        Map.of("taskId", saved.getId().toString()));
  }

  @Override
  @Transactional
  public TaskResponse restoreTask(UUID taskId) {
    User user = securityUtils.getCurrentUser();
    Task task =
        taskRepository
            .findByIdAndOrganizationIdWithUsers(taskId, user.getOrganization().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
    assertCanMutate(task, user);
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
    assertCanView(task, user);
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
    m.put("scope", t.getScope().name());
    m.put("teamId", t.getTeam() != null ? t.getTeam().getId().toString() : null);
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
        .scope(task.getScope())
        .teamId(task.getTeam() != null ? task.getTeam().getId() : null)
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

  private void applyScopeRules(Task task, TaskRequest request, User currentUser) {
    TaskScope requestedScope = request.getScope();
    if (requestedScope == TaskScope.GLOBAL) {
      if (currentUser.getRole() != UserRole.ADMIN) {
        throw new PermissionDeniedException("Only organization admins can create GLOBAL tasks");
      }
      task.setScope(TaskScope.GLOBAL);
      task.setTeam(null);
      return;
    }
    if (requestedScope == TaskScope.TEAM) {
      if (request.getTeamId() == null) {
        throw new BadRequestException("TEAM scope requires teamId");
      }
      Team team =
          teamRepository
              .findByIdAndOrgId(request.getTeamId(), currentUser.getOrganization().getId())
              .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
      task.setScope(TaskScope.TEAM);
      task.setTeam(team);
      return;
    }
    task.setScope(TaskScope.PRIVATE);
    task.setTeam(null);
  }

  private void validateAssigneeForScope(Task task, User assignee) {
    if (task.getScope() != TaskScope.TEAM) {
      return;
    }
    UUID teamId = task.getTeam() != null ? task.getTeam().getId() : null;
    if (teamId == null || !teamMemberRepository.existsByTeamIdAndUserId(teamId, assignee.getId())) {
      throw new BadRequestException("Assignee must be a member of the task team");
    }
  }

  private void assertCanView(Task task, User user) {
    if (!taskVisibilityService.canView(task, user)) {
      throw new PermissionDeniedException("You do not have access to this task");
    }
  }

  private void assertCanMutate(Task task, User user) {
    if (!task.getOrganization().getId().equals(user.getOrganization().getId())) {
      throw new ResourceNotFoundException("Task not found");
    }
    if (user.getRole() == UserRole.ADMIN) {
      return;
    }
    if (task.getCreatedBy().getId().equals(user.getId())) {
      return;
    }
    throw new PermissionDeniedException("You do not have permission");
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
