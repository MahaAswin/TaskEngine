package com.taskengine.backend.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.taskengine.backend.entity.TeamAuditLog;

public interface TeamAuditLogRepository extends JpaRepository<TeamAuditLog, UUID> {

  @Query(
      "select l from TeamAuditLog l "
          + "join fetch l.actor "
          + "left join fetch l.affectedUser "
          + "where l.team.id = :teamId "
          + "order by l.timestamp desc")
  List<TeamAuditLog> findRecentByTeamId(@Param("teamId") UUID teamId);
}
