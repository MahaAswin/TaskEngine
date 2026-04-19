package com.taskengine.backend.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.taskengine.backend.entity.OrgInvite;

public interface OrgInviteRepository extends JpaRepository<OrgInvite, UUID> {}
