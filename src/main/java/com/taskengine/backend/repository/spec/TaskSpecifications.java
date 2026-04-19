package com.taskengine.backend.repository.spec;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import com.taskengine.backend.entity.Task;
import com.taskengine.backend.entity.TaskPriority;
import com.taskengine.backend.entity.TaskStatus;
import com.taskengine.backend.entity.UserRole;

public final class TaskSpecifications {

  private TaskSpecifications() {}

  public static Specification<Task> organizationId(UUID orgId) {
    return (root, query, cb) -> cb.equal(root.get("organization").get("id"), orgId);
  }

  public static Specification<Task> notDeleted() {
    return (root, query, cb) -> cb.isFalse(root.get("deleted"));
  }

  public static Specification<Task> memberCreatedBy(UUID userId) {
    return (root, query, cb) -> cb.equal(root.get("createdBy").get("id"), userId);
  }

  public static Specification<Task> optionalStatus(TaskStatus status) {
    return (root, query, cb) ->
        status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
  }

  public static Specification<Task> optionalPriority(TaskPriority priority) {
    return (root, query, cb) ->
        priority == null ? cb.conjunction() : cb.equal(root.get("priority"), priority);
  }

  public static Specification<Task> optionalAssignedTo(UUID assignedToId) {
    return (root, query, cb) -> {
      if (assignedToId == null) {
        return cb.conjunction();
      }
      return cb.equal(root.get("assignedTo").get("id"), assignedToId);
    };
  }

  public static Specification<Task> search(String search) {
    return (root, query, cb) -> {
      if (search == null || search.isBlank()) {
        return cb.conjunction();
      }
      String pattern = "%" + search.trim().toLowerCase() + "%";
      Expression<String> title = root.get("title");
      Expression<String> desc = root.get("description");
      Expression<String> safeTitle = cb.coalesce(title, cb.literal(""));
      Expression<String> safeDesc = cb.coalesce(desc, cb.literal(""));
      return cb.or(
          cb.like(cb.lower(safeTitle), pattern), cb.like(cb.lower(safeDesc), pattern));
    };
  }

  public static Specification<Task> forUserRole(
      UUID orgId, UserRole role, UUID currentUserId) {
    Specification<Task> spec = organizationId(orgId);
    if (role == UserRole.MEMBER) {
      spec = spec.and(memberCreatedBy(currentUserId));
    }
    return spec;
  }

  /** Fetch joins for list queries (avoid N+1). */
  public static Specification<Task> withFetchUsers() {
    return (root, query, cb) -> {
      if (query.getResultType() != Long.class && query.getResultType() != long.class) {
        query.distinct(true);
        root.fetch("createdBy", JoinType.INNER);
        root.fetch("assignedTo", JoinType.LEFT);
      }
      return cb.conjunction();
    };
  }
}
