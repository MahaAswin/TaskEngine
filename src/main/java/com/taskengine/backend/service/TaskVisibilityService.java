package com.taskengine.backend.service;

import java.util.UUID;
import com.taskengine.backend.entity.Task;
import com.taskengine.backend.entity.User;

public interface TaskVisibilityService {
  boolean canView(Task task, User user);

  boolean canMutate(Task task, User user);

  boolean isTeamLeader(UUID teamId, UUID userId);
}
