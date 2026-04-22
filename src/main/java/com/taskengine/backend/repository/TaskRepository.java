package com.taskengine.backend.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.taskengine.backend.entity.Task;
import com.taskengine.backend.entity.TaskScope;
import com.taskengine.backend.entity.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, UUID>, JpaSpecificationExecutor<Task> {

  @EntityGraph(
      attributePaths = {"createdBy", "assignedTo", "organization", "team"})
  @Query("select t from Task t where t.id = :id and t.organization.id = :orgId")
  Optional<Task> findByIdAndOrganizationIdWithUsers(UUID id, UUID orgId);

  Optional<Task> findByIdAndOrganizationId(UUID id, UUID orgId);

  @Query("select count(t) from Task t where t.organization.id = :orgId")
  long countByOrganizationId(@Param("orgId") UUID orgId);

  @Query("select count(t) from Task t where t.organization.id = :orgId and t.status = :status")
  long countByOrganizationIdAndStatus(
      @Param("orgId") UUID orgId, @Param("status") TaskStatus status);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "update Task t set t.scope = :privateScope, t.team = null "
          + "where t.team.id = :teamId and t.scope = :teamScope")
  int convertTeamTasksToPrivate(
      @Param("teamId") UUID teamId,
      @Param("teamScope") TaskScope teamScope,
      @Param("privateScope") TaskScope privateScope);
}
