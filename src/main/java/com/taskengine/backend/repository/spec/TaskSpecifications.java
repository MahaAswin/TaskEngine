package com.taskengine.backend.repository.spec;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.criteria.JoinType;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import com.taskengine.backend.entity.Task;
import com.taskengine.backend.entity.TaskScope;
import com.taskengine.backend.entity.TaskStatus;
import com.taskengine.backend.entity.TeamMember;

public final class TaskSpecifications {

  private TaskSpecifications() {}

  public static Specification<Task> organizationId(UUID orgId) {
    return (root, query, cb) -> cb.equal(root.get("organization").get("id"), orgId);
  }

  public static Specification<Task> notDeleted() {
    return (root, query, cb) -> cb.isFalse(root.get("deleted"));
  }

  public static Specification<Task> searchContainsTitle(String search) {
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

  public static Specification<Task> optionalStatus(TaskStatus status) {
    return (root, query, cb) ->
        status == null ? cb.conjunction() : cb.equal(root.get("status"), status);
  }

  public static Specification<Task> optionalScope(TaskScope scope) {
    return (root, query, cb) ->
        scope == null ? cb.conjunction() : cb.equal(root.get("scope"), scope);
  }

  public static Specification<Task> optionalTeamId(UUID teamId) {
    return (root, query, cb) ->
        teamId == null ? cb.conjunction() : cb.equal(root.get("team").get("id"), teamId);
  }

  public static Specification<Task> visibility(UUID orgId, UUID userId, boolean isAdmin) {
    return (root, query, cb) -> {
      Predicate notDeleted = cb.isFalse(root.get("deleted"));
      Predicate global = cb.equal(root.get("scope"), TaskScope.GLOBAL);
      Predicate sameOrg = cb.equal(root.get("organization").get("id"), orgId);
      Predicate createdByCurrent = cb.equal(root.get("createdBy").get("id"), userId);
      Predicate assignedToCurrent =
          cb.and(
              cb.isNotNull(root.get("assignedTo")),
              cb.equal(root.get("assignedTo").get("id"), userId));

      Predicate orgScopedVis = cb.and(sameOrg, cb.or(createdByCurrent, assignedToCurrent));

      return cb.and(
          notDeleted,
          cb.or(global, orgScopedVis));
    };
  }

  /** Fetch joins for list queries (avoid N+1) while preserving pagination counts. */
  public static Specification<Task> withFetches() {
    return (root, query, cb) -> {
      if (query.getResultType() != Long.class && query.getResultType() != long.class) {
        query.distinct(true);
        root.fetch("createdBy", JoinType.INNER);
        root.fetch("assignedTo", JoinType.LEFT);
        root.fetch("organization", JoinType.INNER);
        root.fetch("team", JoinType.LEFT);
      }
      return cb.conjunction();
    };
  }
}
