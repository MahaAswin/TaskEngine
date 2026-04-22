package com.taskengine.backend.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.taskengine.backend.entity.Message;

public interface MessageRepository extends JpaRepository<Message, UUID> {

  @EntityGraph(attributePaths = {"sender", "receiver", "organization"})
  @Query(
      """
      SELECT m FROM Message m
      WHERE m.organization.id = :orgId
        AND m.receiver IS NULL
      ORDER BY m.createdAt DESC
      """)
  List<Message> findOrgChannelMessages(@Param("orgId") UUID orgId);

  @EntityGraph(attributePaths = {"sender", "receiver", "organization"})
  @Query(
      """
      SELECT m FROM Message m
      WHERE m.organization.id = :orgId
        AND (
          (m.sender.id = :userId AND m.receiver.id = :peerId)
          OR (m.sender.id = :peerId AND m.receiver.id = :userId)
        )
      ORDER BY m.createdAt DESC
      """)
  List<Message> findDirectMessages(
      @Param("orgId") UUID orgId, @Param("userId") UUID userId, @Param("peerId") UUID peerId);
}
