package com.taskengine.backend.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.taskengine.backend.entity.TeamMember;
import com.taskengine.backend.entity.TeamMemberId;
import com.taskengine.backend.entity.TeamMemberRole;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {

  @Query(
      "select tm from TeamMember tm "
          + "join fetch tm.user u "
          + "where tm.team.id = :teamId and tm.team.organization.id = :orgId "
          + "order by case when tm.role = com.taskengine.backend.entity.TeamMemberRole.TEAM_LEADER then 0 else 1 end, lower(u.fullName)")
  List<TeamMember> findByTeamIdAndOrgId(@Param("teamId") UUID teamId, @Param("orgId") UUID orgId);

  @Query("select count(tm) from TeamMember tm where tm.team.id = :teamId")
  long countByTeamId(@Param("teamId") UUID teamId);

  @Query("select tm.team.id, count(tm) from TeamMember tm where tm.team.id in :teamIds group by tm.team.id")
  List<Object[]> countByTeamIds(@Param("teamIds") Collection<UUID> teamIds);

  @Query("select tm from TeamMember tm where tm.team.id = :teamId and tm.user.id = :userId")
  Optional<TeamMember> findByTeamIdAndUserId(@Param("teamId") UUID teamId, @Param("userId") UUID userId);

  @Query(
      "select case when count(tm) > 0 then true else false end "
          + "from TeamMember tm "
          + "where tm.team.id = :teamId and tm.user.id = :userId")
  boolean existsByTeamIdAndUserId(@Param("teamId") UUID teamId, @Param("userId") UUID userId);

  @Query(
      "select case when count(tm) > 0 then true else false end "
          + "from TeamMember tm "
          + "where tm.team.id = :teamId and tm.user.id = :userId and tm.role = :role")
  boolean existsByTeamIdAndUserIdAndRole(
      @Param("teamId") UUID teamId, @Param("userId") UUID userId, @Param("role") TeamMemberRole role);

  @Query("select count(tm) from TeamMember tm where tm.team.id = :teamId and tm.role = :role")
  long countByTeamIdAndRole(@Param("teamId") UUID teamId, @Param("role") TeamMemberRole role);

  @Query(
      "select tm from TeamMember tm where tm.team.id = :teamId and tm.role = com.taskengine.backend.entity.TeamMemberRole.TEAM_LEADER")
  Optional<TeamMember> findLeaderByTeamId(@Param("teamId") UUID teamId);
}
