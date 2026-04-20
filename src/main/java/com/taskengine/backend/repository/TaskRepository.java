package com.taskengine.backend.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  @EntityGraph(attributePaths = {"createdBy", "assignedTo", "organization", "team"})
  // Visibility rule:
  // - same organization, not deleted
  // - GLOBAL tasks visible to all org users
  // - TEAM tasks visible to org admins or team members
  // - creator/assignee can always see their own/assigned tasks
  @Query(
      """
      SELECT t FROM Task t
      WHERE t.organization.id = :orgId
        AND t.deleted = false
        AND (
          t.scope = :globalScope
          OR (t.scope = :teamScope
              AND ( :isAdmin = true
                    OR EXISTS (
                      SELECT tm FROM TeamMember tm
                      WHERE tm.team.id = t.team.id
                        AND tm.user.id = :userId
                    )))
          OR (t.scope = :privateScope AND t.createdBy.id = :userId)
          OR t.createdBy.id = :userId
          OR t.assignedTo.id = :userId
        )
        AND t.status = :status
        AND (:teamId IS NULL OR t.team.id = :teamId)
        AND LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
      ORDER BY t.createdAt DESC
      """)
  Page<Task> findVisibleTasks(
      @Param("orgId") UUID orgId,
      @Param("userId") UUID userId,
      @Param("isAdmin") boolean isAdmin,
      @Param("globalScope") TaskScope globalScope,
      @Param("teamScope") TaskScope teamScope,
      @Param("privateScope") TaskScope privateScope,
      @Param("status") TaskStatus status,
      @Param("teamId") UUID teamId,
      @Param("search") String search,
      Pageable pageable);

  @EntityGraph(attributePaths = {"createdBy", "assignedTo", "organization", "team"})
  @Query(
      """
      SELECT t FROM Task t
      WHERE t.organization.id = :orgId
        AND t.deleted = false
        AND (
          t.scope = :globalScope
          OR (t.scope = :teamScope
              AND ( :isAdmin = true
                    OR EXISTS (
                      SELECT tm FROM TeamMember tm
                      WHERE tm.team.id = t.team.id
                        AND tm.user.id = :userId
                    )))
          OR (t.scope = :privateScope AND t.createdBy.id = :userId)
          OR t.createdBy.id = :userId
          OR t.assignedTo.id = :userId
        )
        AND t.status = :status
        AND (:teamId IS NULL OR t.team.id = :teamId)
      ORDER BY t.createdAt DESC
      """)
  Page<Task> findVisibleTasksNoSearch(
      @Param("orgId") UUID orgId,
      @Param("userId") UUID userId,
      @Param("isAdmin") boolean isAdmin,
      @Param("globalScope") TaskScope globalScope,
      @Param("teamScope") TaskScope teamScope,
      @Param("privateScope") TaskScope privateScope,
      @Param("status") TaskStatus status,
      @Param("teamId") UUID teamId,
      Pageable pageable);

  @EntityGraph(attributePaths = {"createdBy", "assignedTo", "organization", "team"})
  @Query(
      """
      SELECT t FROM Task t
      WHERE t.organization.id = :orgId
        AND t.deleted = false
        AND (
          t.scope = :globalScope
          OR (t.scope = :teamScope
              AND ( :isAdmin = true
                    OR EXISTS (
                      SELECT tm FROM TeamMember tm
                      WHERE tm.team.id = t.team.id
                        AND tm.user.id = :userId
                    )))
          OR (t.scope = :privateScope AND t.createdBy.id = :userId)
          OR t.createdBy.id = :userId
          OR t.assignedTo.id = :userId
        )
        AND (:teamId IS NULL OR t.team.id = :teamId)
        AND LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%'))
      ORDER BY t.createdAt DESC
      """)
  Page<Task> findVisibleTasksNoStatus(
      @Param("orgId") UUID orgId,
      @Param("userId") UUID userId,
      @Param("isAdmin") boolean isAdmin,
      @Param("globalScope") TaskScope globalScope,
      @Param("teamScope") TaskScope teamScope,
      @Param("privateScope") TaskScope privateScope,
      @Param("teamId") UUID teamId,
      @Param("search") String search,
      Pageable pageable);

  @EntityGraph(attributePaths = {"createdBy", "assignedTo", "organization", "team"})
  @Query(
      """
      SELECT t FROM Task t
      WHERE t.organization.id = :orgId
        AND t.deleted = false
        AND (
          t.scope = :globalScope
          OR (t.scope = :teamScope
              AND ( :isAdmin = true
                    OR EXISTS (
                      SELECT tm FROM TeamMember tm
                      WHERE tm.team.id = t.team.id
                        AND tm.user.id = :userId
                    )))
          OR (t.scope = :privateScope AND t.createdBy.id = :userId)
          OR t.createdBy.id = :userId
          OR t.assignedTo.id = :userId
        )
        AND (:teamId IS NULL OR t.team.id = :teamId)
      ORDER BY t.createdAt DESC
      """)
  Page<Task> findVisibleTasksNoStatusNoSearch(
      @Param("orgId") UUID orgId,
      @Param("userId") UUID userId,
      @Param("isAdmin") boolean isAdmin,
      @Param("globalScope") TaskScope globalScope,
      @Param("teamScope") TaskScope teamScope,
      @Param("privateScope") TaskScope privateScope,
      @Param("teamId") UUID teamId,
      Pageable pageable);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(
      "update Task t set t.scope = :privateScope, t.team = null "
          + "where t.team.id = :teamId and t.scope = :teamScope")
  int convertTeamTasksToPrivate(
      @Param("teamId") UUID teamId,
      @Param("teamScope") TaskScope teamScope,
      @Param("privateScope") TaskScope privateScope);
}
