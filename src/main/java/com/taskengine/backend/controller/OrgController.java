package com.taskengine.backend.controller;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.taskengine.backend.dto.InviteRequest;
import com.taskengine.backend.dto.OrgMemberResponse;
import com.taskengine.backend.dto.PatchOrgRequest;
import com.taskengine.backend.service.OrgService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/org")
@Validated
@RequiredArgsConstructor
@Tag(name = "Organization")
public class OrgController {

  private final OrgService orgService;

  @GetMapping("/members")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "List organization members (admin only)")
  public List<OrgMemberResponse> members() {
    return orgService.listMembers();
  }

  @PostMapping("/invite")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Invite user by email (admin only)")
  public void invite(@Valid @RequestBody InviteRequest request) {
    orgService.inviteMember(request);
  }

  @PatchMapping("/settings")
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Update organization name (admin only)")
  public void settings(@Valid @RequestBody PatchOrgRequest request) {
    orgService.updateOrganization(request);
  }
}
