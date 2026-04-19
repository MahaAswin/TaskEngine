package com.taskengine.backend.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.taskengine.backend.dto.*;
import com.taskengine.backend.entity.TaskPriority;
import com.taskengine.backend.entity.TaskStatus;
import com.taskengine.backend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tasks")
@Validated
@RequiredArgsConstructor
@Tag(name = "Tasks")
public class TaskController {

  private final TaskService taskService;

  @GetMapping
  @Operation(summary = "List tasks with filters and sorting")
  @ApiResponse(responseCode = "200", description = "OK")
  public Page<TaskResponse> list(
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) TaskPriority priority,
      @RequestParam(required = false) UUID assignedTo,
      @RequestParam(required = false) String search,
      @PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
          Pageable pageable) {
    return taskService.searchTasks(status, priority, assignedTo, search, pageable);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get task by id")
  @ApiResponse(responseCode = "200", description = "OK")
  public TaskResponse get(@PathVariable UUID id) {
    return taskService.getTask(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create task")
  @ApiResponse(responseCode = "201", description = "Created")
  public TaskResponse create(@Valid @RequestBody TaskRequest request) {
    return taskService.createTask(request);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Replace task")
  @ApiResponse(responseCode = "200", description = "OK")
  public TaskResponse put(@PathVariable UUID id, @Valid @RequestBody TaskRequest request) {
    return taskService.updateTask(id, request);
  }

  @PatchMapping("/{id}/status")
  @Operation(summary = "Patch task status")
  @ApiResponse(responseCode = "200", description = "OK")
  public TaskResponse patchStatus(
      @PathVariable UUID id, @Valid @RequestBody PatchTaskStatusRequest request) {
    return taskService.patchTaskStatus(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Soft-delete task")
  @ApiResponse(responseCode = "204", description = "No content")
  public void delete(@PathVariable UUID id) {
    taskService.deleteTask(id);
  }

  @PostMapping("/{id}/restore")
  @Operation(summary = "Restore a soft-deleted task")
  @ApiResponse(responseCode = "200", description = "OK")
  public TaskResponse restore(@PathVariable UUID id) {
    return taskService.restoreTask(id);
  }

  @GetMapping("/{id}/history")
  @Operation(summary = "Audit history for task")
  @ApiResponse(responseCode = "200", description = "OK")
  public List<TaskHistoryEntryResponse> history(@PathVariable UUID id) {
    return taskService.getTaskHistory(id);
  }
}
