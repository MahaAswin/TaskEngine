package com.taskengine.backend.service.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import com.taskengine.backend.entity.Task;
import com.taskengine.backend.entity.TaskScope;
import com.taskengine.backend.entity.TeamMemberRole;
import com.taskengine.backend.entity.User;
import com.taskengine.backend.entity.UserRole;
import com.taskengine.backend.repository.TeamMemberRepository;
import com.taskengine.backend.service.TaskVisibilityService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskVisibilityServiceImpl implements TaskVisibilityService {

  private final TeamMemberRepository teamMemberRepository;

  @Override
  public boolean canView(Task task, User user) {
    if (task.isDeleted()) {
      return false;
    }
    if (task.getScope() == TaskScope.GLOBAL) {
      return true;
    }
    if (!task.getOrganization().getId().equals(user.getOrganization().getId())) {
      return false;
    }
    // Assignees are allowed to access tasks assigned to them across scopes.
    if (task.getAssignedTo() != null && task.getAssignedTo().getId().equals(user.getId())) {
      return true;
    }
    if (task.getScope() == TaskScope.GLOBAL) {
      return true;
    }
    if (task.getScope() == TaskScope.PRIVATE) {
      return task.getCreatedBy().getId().equals(user.getId());
    }
    if (user.getRole() == UserRole.ADMIN) {
      return true;
    }
    if (task.getTeam() == null) {
      return false;
    }
    return teamMemberRepository.existsByTeamIdAndUserId(task.getTeam().getId(), user.getId());
  }

  @Override
  public boolean canMutate(Task task, User user) {
    if (!task.getOrganization().getId().equals(user.getOrganization().getId()) || task.isDeleted()) {
      return false;
    }
    if (task.getScope() == TaskScope.GLOBAL) {
      return user.getRole() == UserRole.ADMIN;
    }
    if (task.getScope() == TaskScope.PRIVATE) {
      return task.getCreatedBy().getId().equals(user.getId());
    }
    if (user.getRole() == UserRole.ADMIN) {
      return true;
    }
    if (task.getTeam() == null) {
      return false;
    }
    return teamMemberRepository.existsByTeamIdAndUserIdAndRole(
        task.getTeam().getId(), user.getId(), TeamMemberRole.TEAM_LEADER);
  }

  @Override
  public boolean isTeamLeader(UUID teamId, UUID userId) {
    return teamMemberRepository.existsByTeamIdAndUserIdAndRole(
        teamId, userId, TeamMemberRole.TEAM_LEADER);
  }
}
