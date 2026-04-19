package com.taskengine.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.taskengine.backend.dto.TaskStatsResponse;
import com.taskengine.backend.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Task statistics")
public class TaskStatsController {

  private final TaskService taskService;

  @GetMapping("/tasks/stats")
  @Operation(summary = "Aggregated task counts for current user visibility")
  public TaskStatsResponse taskStats() {
    return taskService.getTaskStats();
  }
}
