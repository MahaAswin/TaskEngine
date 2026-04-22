package com.taskengine.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.taskengine.backend.entity.Team;

public interface TeamRepository extends JpaRepository<Team, UUID> {

  @Query("select t from Team t where t.organization.id = :orgId order by t.createdAt desc")
  List<Team> findByOrganizationId(@Param("orgId") UUID orgId);

  @Query(
      "select t from Team t "
          + "join TeamMember tm on tm.team.id = t.id "
          + "where t.organization.id = :orgId and tm.user.id = :userId "
          + "order by t.createdAt desc")
  List<Team> findByOrganizationIdAndMemberId(
      @Param("orgId") UUID orgId, @Param("userId") UUID userId);

  @EntityGraph(attributePaths = {"organization", "createdBy"})
  @Query("select t from Team t where t.id = :teamId and t.organization.id = :orgId")
  Optional<Team> findByIdAndOrgId(@Param("teamId") UUID teamId, @Param("orgId") UUID orgId);

  boolean existsByOrganization_IdAndNameIgnoreCase(UUID orgId, String name);

  boolean existsByInviteCodeIgnoreCase(String inviteCode);

  @EntityGraph(attributePaths = {"organization", "createdBy"})
  Optional<Team> findByOrganization_IdAndInviteCodeIgnoreCase(UUID orgId, String inviteCode);
}
