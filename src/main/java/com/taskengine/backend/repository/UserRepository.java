package com.taskengine.backend.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.taskengine.backend.entity.User;

public interface UserRepository extends JpaRepository<User, UUID> {

  @Query("select u from User u join fetch u.organization where u.email = :email")
  Optional<User> findByEmailWithOrganization(@Param("email") String email);

  @Query("select u from User u join fetch u.organization where u.googleSub = :sub")
  Optional<User> findByGoogleSubWithOrganization(@Param("sub") String sub);

  @Query(
      "select u from User u join fetch u.organization o where u.id = :id and o.id = :orgId")
  Optional<User> findByIdAndOrganizationId(
      @Param("id") UUID id, @Param("orgId") UUID orgId);

  @Query("select u from User u join fetch u.organization where u.id = :id")
  Optional<User> findByIdWithOrganization(@Param("id") UUID id);

  boolean existsByEmail(String email);

  List<User> findByOrganization_IdOrderByFullNameAsc(UUID organizationId);

  @Query(
      "select count(t) from Task t where t.organization.id = :orgId and t.createdBy.id = :userId and t.deleted = false")
  long countTasksCreatedByUserInOrg(
      @Param("orgId") UUID orgId, @Param("userId") UUID userId);
}
