package com.taskengine.backend.service;

import java.util.List;
import java.util.UUID;
import com.taskengine.backend.dto.AddTeamMemberRequest;
import com.taskengine.backend.dto.CreateTeamRequest;
import com.taskengine.backend.dto.TeamActivityEntryResponse;
import com.taskengine.backend.dto.TeamCandidateResponse;
import com.taskengine.backend.dto.TeamDetailResponse;
import com.taskengine.backend.dto.TeamResponse;
import com.taskengine.backend.dto.UpdateTeamMemberRoleRequest;

public interface TeamService {
  List<TeamResponse> listTeams();

  TeamResponse createTeam(CreateTeamRequest request);

  TeamDetailResponse getTeam(UUID teamId);

  List<TeamCandidateResponse> listCandidates(UUID teamId);

  List<TeamActivityEntryResponse> getTeamActivity(UUID teamId);

  void addMember(UUID teamId, AddTeamMemberRequest request);

  void removeMember(UUID teamId, UUID userId);

  void updateMemberRole(UUID teamId, UUID userId, UpdateTeamMemberRoleRequest request);

  void deleteTeam(UUID teamId);
}
