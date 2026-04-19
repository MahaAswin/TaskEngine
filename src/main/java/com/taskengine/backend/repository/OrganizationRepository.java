package com.taskengine.backend.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.taskengine.backend.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

  Optional<Organization> findBySlug(String slug);

  boolean existsBySlug(String slug);
}
