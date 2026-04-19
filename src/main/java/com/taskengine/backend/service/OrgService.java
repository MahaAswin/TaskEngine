package com.taskengine.backend.service;

import java.util.List;
import com.taskengine.backend.dto.InviteRequest;
import com.taskengine.backend.dto.OrgMemberResponse;
import com.taskengine.backend.dto.PatchOrgRequest;

public interface OrgService {

  List<OrgMemberResponse> listMembers();

  void inviteMember(InviteRequest request);

  void updateOrganization(PatchOrgRequest request);
}
