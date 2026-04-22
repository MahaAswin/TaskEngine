package com.taskengine.backend.service.impl;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.taskengine.backend.dto.InviteRequest;
import com.taskengine.backend.dto.OrgMemberResponse;
import com.taskengine.backend.dto.PatchOrgRequest;
import com.taskengine.backend.entity.Organization;
import com.taskengine.backend.entity.User;
import com.taskengine.backend.entity.UserRole;
import com.taskengine.backend.exception.PermissionDeniedException;
import com.taskengine.backend.repository.OrganizationRepository;
import com.taskengine.backend.repository.UserRepository;
import com.taskengine.backend.service.InviteService;
import com.taskengine.backend.service.OrgService;
import com.taskengine.backend.util.SecurityUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrgServiceImpl implements OrgService {

  private final UserRepository userRepository;
  private final OrganizationRepository organizationRepository;
  private final SecurityUtils securityUtils;
  private final InviteService inviteService;

  @Override
  @Transactional(readOnly = true)
  public List<OrgMemberResponse> listMembers() {
    User admin = securityUtils.getCurrentUser();
    if (admin.getRole() != UserRole.ADMIN) {
      throw new PermissionDeniedException("Only administrators can list members");
    }
    var orgId = admin.getOrganization().getId();
    return userRepository.findByOrganization_IdOrderByFullNameAsc(orgId).stream()
        .map(
            u ->
                OrgMemberResponse.builder()
                    .id(u.getId())
                    .fullName(u.getFullName())
                    .email(u.getEmail())
                    .role(u.getRole())
                    .taskCount(0L)
                    .build())
        .toList();
  }

  @Override
  @Transactional
  public void inviteMember(InviteRequest request) {
    User admin = securityUtils.getCurrentUser();
    if (admin.getRole() != UserRole.ADMIN) {
      throw new PermissionDeniedException("Only administrators can invite members");
    }
    inviteService.sendInvite(request);
  }

  @Override
  @Transactional
  public void updateOrganization(PatchOrgRequest request) {
    User admin = securityUtils.getCurrentUser();
    if (admin.getRole() != UserRole.ADMIN) {
      throw new PermissionDeniedException("Only administrators can update organization settings");
    }
    Organization org =
        organizationRepository
            .findById(admin.getOrganization().getId())
            .orElseThrow(() -> new IllegalStateException("Organization missing"));
    org.setName(request.getName());
    organizationRepository.save(org);
  }
}
