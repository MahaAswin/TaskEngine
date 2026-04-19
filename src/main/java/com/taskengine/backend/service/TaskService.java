package com.taskengine.backend.service;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.taskengine.backend.dto.*;
import com.taskengine.backend.entity.TaskPriority;
import com.taskengine.backend.entity.TaskStatus;
import com.taskengine.backend.security.TaskPermission;
import com.taskengine.backend.security.TaskPermissionAction;

public interface TaskService {

  TaskStatsResponse getTaskStats();

  Page<TaskResponse> searchTasks(
      TaskStatus status,
      TaskPriority priority,
      UUID assignedTo,
      String search,
      Pageable pageable);

  @TaskPermission(TaskPermissionAction.READ)
  TaskResponse getTask(UUID taskId);

  TaskResponse createTask(TaskRequest request);

  @TaskPermission(TaskPermissionAction.WRITE)
  TaskResponse updateTask(UUID taskId, TaskRequest request);

  @TaskPermission(TaskPermissionAction.WRITE)
  TaskResponse patchTaskStatus(UUID taskId, PatchTaskStatusRequest request);

  @TaskPermission(TaskPermissionAction.DELETE)
  void deleteTask(UUID taskId);

  @TaskPermission(TaskPermissionAction.WRITE)
  TaskResponse restoreTask(UUID taskId);

  @TaskPermission(TaskPermissionAction.READ)
  List<TaskHistoryEntryResponse> getTaskHistory(UUID taskId);
}
