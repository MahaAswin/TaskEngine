package com.taskengine.backend.controller;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.taskengine.backend.dto.*;
import com.taskengine.backend.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/teams")
@Validated
@RequiredArgsConstructor
@Tag(name = "Teams")
public class TeamController {

  private final TeamService teamService;

  @GetMapping
  @Operation(summary = "List teams in the current organization")
  public List<TeamResponse> listTeams() {
    return teamService.listTeams();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a team")
  public TeamResponse createTeam(@Valid @RequestBody CreateTeamRequest request) {
    return teamService.createTeam(request);
  }

  @GetMapping("/my")
  @Operation(summary = "Get teams for current user")
  public List<TeamResponse> getMyTeams() {
    return teamService.listTeams();
  }

  @PostMapping("/join")
  @Operation(summary = "Join team via invite code")
  public TeamResponse joinTeam(@Valid @RequestBody JoinTeamRequest request) {
    return teamService.joinTeamByInviteCode(request.getInviteCode());
  }

  @GetMapping("/{teamId}")
  @Operation(summary = "Get team details with members")
  public TeamDetailResponse getTeam(@PathVariable UUID teamId) {
    return teamService.getTeam(teamId);
  }

  @GetMapping("/{teamId}/candidates")
  @Operation(summary = "List organization users who can be added to this team")
  public List<TeamCandidateResponse> listCandidates(@PathVariable UUID teamId) {
    return teamService.listCandidates(teamId);
  }

  @GetMapping("/{teamId}/activity")
  @Operation(summary = "Get team activity feed")
  public List<TeamActivityEntryResponse> getTeamActivity(@PathVariable UUID teamId) {
    return teamService.getTeamActivity(teamId);
  }

  @PostMapping("/{teamId}/members")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Add a member to a team")
  public void addMember(@PathVariable UUID teamId, @Valid @RequestBody AddTeamMemberRequest request) {
    teamService.addMember(teamId, request);
  }

  @DeleteMapping("/{teamId}/members/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Remove member from a team")
  public void removeMember(@PathVariable UUID teamId, @PathVariable UUID userId) {
    teamService.removeMember(teamId, userId);
  }

  @PatchMapping("/{teamId}/members/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Update member role")
  public void updateMemberRole(
      @PathVariable UUID teamId,
      @PathVariable UUID userId,
      @Valid @RequestBody UpdateTeamMemberRoleRequest request) {
    teamService.updateMemberRole(teamId, userId, request);
  }

  @DeleteMapping("/{teamId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Delete team")
  public void deleteTeam(@PathVariable UUID teamId) {
    teamService.deleteTeam(teamId);
  }
}
