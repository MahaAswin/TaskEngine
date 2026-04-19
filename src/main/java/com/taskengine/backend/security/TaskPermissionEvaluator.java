package com.taskengine.backend.security;

import org.springframework.stereotype.Component;
import com.taskengine.backend.entity.Task;
import com.taskengine.backend.entity.UserRole;
import com.taskengine.backend.exception.PermissionDeniedException;

@Component
public class TaskPermissionEvaluator {

  public void assertTenant(Task task, UserPrincipal principal) {
    if (task.getOrganization() == null
        || !task.getOrganization().getId().equals(principal.getOrgId())) {
      throw new PermissionDeniedException("Task is not in your organization");
    }
  }

  /** ADMIN: any task in org. MEMBER: only tasks they created. */
  public void assertCanRead(Task task, UserPrincipal principal) {
    assertTenant(task, principal);
    if (principal.getRole() == UserRole.ADMIN) {
      return;
    }
    if (task.getCreatedBy().getId().equals(principal.getId())) {
      return;
    }
    throw new PermissionDeniedException("You can only view tasks you created");
  }

  /** ADMIN: any task. MEMBER: only own tasks; cannot change assignment (use assertCanAssign). */
  public void assertCanWrite(Task task, UserPrincipal principal) {
    assertTenant(task, principal);
    if (principal.getRole() == UserRole.ADMIN) {
      return;
    }
    if (task.getCreatedBy().getId().equals(principal.getId())) {
      return;
    }
    throw new PermissionDeniedException("You can only edit tasks you created");
  }

  public void assertCanDelete(Task task, UserPrincipal principal) {
    assertCanWrite(task, principal);
  }

  public void assertCanAssign(UserPrincipal principal) {
    if (principal.getRole() != UserRole.ADMIN) {
      throw new PermissionDeniedException("Only organization admins can assign tasks");
    }
  }

  /** MEMBER cannot change assignee; ADMIN can. */
  public void assertAssigneeChangeAllowed(
      UserPrincipal principal, boolean assigneeChanged) {
    if (!assigneeChanged) {
      return;
    }
    assertCanAssign(principal);
  }
}
