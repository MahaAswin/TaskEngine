package com.taskengine.backend.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.taskengine.backend.entity.TaskAuditLog;

public interface TaskAuditLogRepository extends JpaRepository<TaskAuditLog, UUID> {

  @Query(
      "select l from TaskAuditLog l join fetch l.actor join l.task t where t.id = :taskId and t.organization.id = :orgId order by l.timestamp desc")
  List<TaskAuditLog> findByTaskIdAndOrgId(
      @Param("taskId") UUID taskId, @Param("orgId") UUID orgId);
}
