package com.taskengine.backend.service.impl;

import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskengine.backend.dto.*;
import com.taskengine.backend.entity.*;
import com.taskengine.backend.exception.BadRequestException;
import com.taskengine.backend.exception.PermissionDeniedException;
import com.taskengine.backend.exception.ResourceNotFoundException;
import com.taskengine.backend.repository.TaskAuditLogRepository;
import com.taskengine.backend.repository.TaskRepository;
import com.taskengine.backend.repository.TeamAuditLogRepository;
import com.taskengine.backend.repository.TeamMemberRepository;
import com.taskengine.backend.repository.TeamRepository;
import com.taskengine.backend.repository.UserRepository;
import com.taskengine.backend.service.AuditLogService;
import com.taskengine.backend.service.TeamService;
import com.taskengine.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

  private final TeamRepository teamRepository;
  private final TeamMemberRepository teamMemberRepository;
  private final UserRepository userRepository;
  private final TaskRepository taskRepository;
  private final TaskAuditLogRepository taskAuditLogRepository;
  private final TeamAuditLogRepository teamAuditLogRepository;
  private final AuditLogService auditLogService;
  private final SecurityUtils securityUtils;

  @Override
  @Transactional(readOnly = true)
  public List<TeamResponse> listTeams() {
    User current = securityUtils.getCurrentUser();
    List<Team> teams =
        teamRepository.findByOrganizationIdAndMemberId(
            current.getOrganization().getId(), current.getId());
    Map<UUID, Long> memberCounts = memberCounts(teams);
    return teams.stream()
        .map(
            t ->
                TeamResponse.builder()
                    .id(t.getId())
                    .name(t.getName())
                    .description(t.getDescription())
                    .memberCount(memberCounts.getOrDefault(t.getId(), 0L))
                    .createdBy(t.getCreatedBy().getId())
                    .createdAt(t.getCreatedAt())
                    .myRole(findMyRole(t.getId(), current.getId()))
                    .build())
        .toList();
  }

  @Override
  @Transactional
  public TeamResponse createTeam(CreateTeamRequest request) {
    User current = securityUtils.getCurrentUser();
    assertAdmin(current);
    String normalizedName = request.getName().trim();
    if (teamRepository.existsByOrganization_IdAndNameIgnoreCase(
        current.getOrganization().getId(), normalizedName)) {
      throw new BadRequestException("A team with this name already exists in your organization");
    }

    Team team = new Team();
    team.setOrganization(current.getOrganization());
    team.setName(normalizedName);
    team.setDescription(request.getDescription());
    team.setCreatedBy(current);
    Team saved = teamRepository.save(team);

    TeamMember leader = new TeamMember();
    leader.setId(new TeamMemberId(saved.getId(), current.getId()));
    leader.setTeam(saved);
    leader.setUser(current);
    leader.setRole(TeamMemberRole.TEAM_LEADER);
    teamMemberRepository.save(leader);
    auditLogService.logTeamAction(saved.getId(), current.getId(), TeamAuditAction.TEAM_CREATED, null);

    if (request.getMemberIds() != null) {
      for (UUID memberId : new LinkedHashSet<>(request.getMemberIds())) {
        if (memberId.equals(current.getId())) {
          continue;
        }
        User member = resolveOrgUser(memberId, current.getOrganization().getId());
        TeamMember tm = new TeamMember();
        tm.setId(new TeamMemberId(saved.getId(), member.getId()));
        tm.setTeam(saved);
        tm.setUser(member);
        tm.setRole(TeamMemberRole.MEMBER);
        teamMemberRepository.save(tm);
        auditLogService.logTeamAction(saved.getId(), current.getId(), TeamAuditAction.TEAM_MEMBER_ADDED, member.getId());
      }
    }

    long memberCount = teamMemberRepository.countByTeamId(saved.getId());
    return TeamResponse.builder()
        .id(saved.getId())
        .name(saved.getName())
        .description(saved.getDescription())
        .memberCount(memberCount)
        .createdBy(saved.getCreatedBy().getId())
        .createdAt(saved.getCreatedAt())
        .myRole(TeamMemberRole.TEAM_LEADER)
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public TeamDetailResponse getTeam(UUID teamId) {
    User current = securityUtils.getCurrentUser();
    Team team = resolveTeam(teamId, current.getOrganization().getId());
    assertCanViewTeam(current, team.getId());
    List<TeamMember> members =
        teamMemberRepository.findByTeamIdAndOrgId(team.getId(), current.getOrganization().getId());
    return TeamDetailResponse.builder()
        .id(team.getId())
        .name(team.getName())
        .description(team.getDescription())
        .memberCount(members.size())
        .createdBy(team.getCreatedBy().getId())
        .createdAt(team.getCreatedAt())
        .myRole(findMyRole(team.getId(), current.getId()))
        .members(members.stream().map(this::toMemberDto).toList())
        .build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<TeamCandidateResponse> listCandidates(UUID teamId) {
    User current = securityUtils.getCurrentUser();
    Team team = resolveTeam(teamId, current.getOrganization().getId());
    assertAdminOrLeader(current, team.getId());

    Set<UUID> existingMemberIds =
        teamMemberRepository.findByTeamIdAndOrgId(team.getId(), current.getOrganization().getId()).stream()
            .map(tm -> tm.getUser().getId())
            .collect(java.util.stream.Collectors.toSet());

    return userRepository.findByOrganization_IdOrderByFullNameAsc(current.getOrganization().getId()).stream()
        .filter(u -> !existingMemberIds.contains(u.getId()))
        .map(
            u ->
                TeamCandidateResponse.builder()
                    .id(u.getId())
                    .fullName(u.getFullName())
                    .email(u.getEmail())
                    .build())
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<TeamActivityEntryResponse> getTeamActivity(UUID teamId) {
    User current = securityUtils.getCurrentUser();
    Team team = resolveTeam(teamId, current.getOrganization().getId());
    assertCanViewTeam(current, team.getId());

    List<TeamActivityEntryResponse> taskEntries =
        taskAuditLogRepository.findTeamActivity(current.getOrganization().getId(), team.getId()).stream()
            .map(
                log ->
                    TeamActivityEntryResponse.builder()
                        .id(log.getId())
                        .source("TASK")
                        .action(log.getAction().name())
                        .taskId(log.getTask().getId())
                        .taskTitle(log.getTask().getTitle())
                        .actorId(log.getActor().getId())
                        .actorName(log.getActor().getFullName())
                        .timestamp(log.getTimestamp())
                        .build())
            .toList();

    List<TeamActivityEntryResponse> teamEntries =
        teamAuditLogRepository.findRecentByTeamId(team.getId()).stream()
            .map(
                log ->
                    TeamActivityEntryResponse.builder()
                        .id(log.getId())
                        .source("TEAM")
                        .action(log.getAction().name())
                        .actorId(log.getActor().getId())
                        .actorName(log.getActor().getFullName())
                        .affectedUserId(log.getAffectedUser() != null ? log.getAffectedUser().getId() : null)
                        .affectedUserName(log.getAffectedUser() != null ? log.getAffectedUser().getFullName() : null)
                        .timestamp(log.getTimestamp())
                        .build())
            .toList();

    return java.util.stream.Stream.concat(taskEntries.stream(), teamEntries.stream())
        .sorted(Comparator.comparing(TeamActivityEntryResponse::getTimestamp).reversed())
        .limit(50)
        .toList();
  }

  @Override
  @Transactional
  public void addMember(UUID teamId, AddTeamMemberRequest request) {
    User current = securityUtils.getCurrentUser();
    Team team = resolveTeam(teamId, current.getOrganization().getId());
    assertAdminOrLeader(current, team.getId());
    User userToAdd = resolveOrgUser(request.getUserId(), current.getOrganization().getId());
    if (teamMemberRepository.existsByTeamIdAndUserId(team.getId(), userToAdd.getId())) {
      return;
    }
    TeamMember tm = new TeamMember();
    tm.setId(new TeamMemberId(team.getId(), userToAdd.getId()));
    tm.setTeam(team);
    tm.setUser(userToAdd);
    tm.setRole(TeamMemberRole.MEMBER);
    teamMemberRepository.save(tm);
    auditLogService.logTeamAction(team.getId(), current.getId(), TeamAuditAction.TEAM_MEMBER_ADDED, userToAdd.getId());
  }

  @Override
  @Transactional
  public void removeMember(UUID teamId, UUID userId) {
    User current = securityUtils.getCurrentUser();
    Team team = resolveTeam(teamId, current.getOrganization().getId());
    assertAdminOrLeader(current, team.getId());
    TeamMember member =
        teamMemberRepository
            .findByTeamIdAndUserId(team.getId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));
    if (member.getRole() == TeamMemberRole.TEAM_LEADER) {
      throw new BadRequestException("Team leader cannot be removed. Assign a new leader first.");
    }
    teamMemberRepository.delete(member);
    auditLogService.logTeamAction(team.getId(), current.getId(), TeamAuditAction.TEAM_MEMBER_REMOVED, member.getUser().getId());
  }

  @Override
  @Transactional
  public void updateMemberRole(UUID teamId, UUID userId, UpdateTeamMemberRoleRequest request) {
    User current = securityUtils.getCurrentUser();
    Team team = resolveTeam(teamId, current.getOrganization().getId());
    assertAdmin(current);
    TeamMember target =
        teamMemberRepository
            .findByTeamIdAndUserId(team.getId(), userId)
            .orElseThrow(() -> new ResourceNotFoundException("Team member not found"));

    if (request.getRole() == TeamMemberRole.TEAM_LEADER) {
      TeamMember currentLeader =
          teamMemberRepository
              .findLeaderByTeamId(team.getId())
              .orElseThrow(() -> new IllegalStateException("Team has no leader"));
      currentLeader.setRole(TeamMemberRole.MEMBER);
      target.setRole(TeamMemberRole.TEAM_LEADER);
      teamMemberRepository.save(currentLeader);
      teamMemberRepository.save(target);
      auditLogService.logTeamAction(team.getId(), current.getId(), TeamAuditAction.TEAM_MEMBER_ROLE_CHANGED, target.getUser().getId());
      return;
    }

    long leaderCount = teamMemberRepository.countByTeamIdAndRole(team.getId(), TeamMemberRole.TEAM_LEADER);
    if (target.getRole() == TeamMemberRole.TEAM_LEADER && leaderCount <= 1) {
      throw new BadRequestException("A team must have exactly one TEAM_LEADER");
    }
    target.setRole(request.getRole());
    teamMemberRepository.save(target);
    auditLogService.logTeamAction(team.getId(), current.getId(), TeamAuditAction.TEAM_MEMBER_ROLE_CHANGED, target.getUser().getId());
  }

  @Override
  @Transactional
  public void deleteTeam(UUID teamId) {
    User current = securityUtils.getCurrentUser();
    Team team = resolveTeam(teamId, current.getOrganization().getId());
    assertAdmin(current);
    taskRepository.convertTeamTasksToPrivate(teamId, TaskScope.TEAM, TaskScope.PRIVATE);
    teamRepository.delete(team);
  }

  private Team resolveTeam(UUID teamId, UUID orgId) {
    return teamRepository
        .findByIdAndOrgId(teamId, orgId)
        .orElseThrow(() -> new ResourceNotFoundException("Team not found"));
  }

  private User resolveOrgUser(UUID userId, UUID orgId) {
    return userRepository
        .findByIdAndOrganizationId(userId, orgId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found in organization"));
  }

  private TeamMemberRole findMyRole(UUID teamId, UUID userId) {
    return teamMemberRepository.findByTeamIdAndUserId(teamId, userId).map(TeamMember::getRole).orElse(null);
  }

  private Map<UUID, Long> memberCounts(List<Team> teams) {
    if (teams.isEmpty()) {
      return Map.of();
    }
    List<UUID> ids = teams.stream().map(Team::getId).toList();
    Map<UUID, Long> out = new HashMap<>();
    for (Object[] row : teamMemberRepository.countByTeamIds(ids)) {
      out.put((UUID) row[0], (Long) row[1]);
    }
    return out;
  }

  private TeamMemberResponse toMemberDto(TeamMember member) {
    return TeamMemberResponse.builder()
        .userId(member.getUser().getId())
        .fullName(member.getUser().getFullName())
        .avatarUrl(member.getUser().getAvatarUrl())
        .role(member.getRole())
        .joinedAt(member.getJoinedAt())
        .build();
  }

  private void assertAdmin(User user) {
    if (user.getRole() != UserRole.ADMIN) {
      throw new PermissionDeniedException("Only administrators can perform this action");
    }
  }

  private void assertAdminOrLeader(User user, UUID teamId) {
    if (user.getRole() == UserRole.ADMIN) {
      return;
    }
    if (teamMemberRepository.existsByTeamIdAndUserIdAndRole(teamId, user.getId(), TeamMemberRole.TEAM_LEADER)) {
      return;
    }
    throw new PermissionDeniedException("Only admins or team leaders can perform this action");
  }

  private void assertCanViewTeam(User user, UUID teamId) {
    if (user.getRole() == UserRole.ADMIN) {
      return;
    }
    if (teamMemberRepository.existsByTeamIdAndUserId(teamId, user.getId())) {
      return;
    }
    throw new PermissionDeniedException("You are not a member of this team");
  }
}
