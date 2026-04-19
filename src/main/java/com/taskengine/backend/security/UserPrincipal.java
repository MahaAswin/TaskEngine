package com.taskengine.backend.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.taskengine.backend.entity.User;
import com.taskengine.backend.entity.UserRole;
import lombok.Getter;

@Getter
public class UserPrincipal implements UserDetails {

  private final UUID id;
  private final UUID orgId;
  private final String email;
  private final UserRole role;
  private final String passwordHash;
  private final boolean active;

  public UserPrincipal(
      UUID id,
      UUID orgId,
      String email,
      UserRole role,
      String passwordHash,
      boolean active) {
    this.id = id;
    this.orgId = orgId;
    this.email = email;
    this.role = role;
    this.passwordHash = passwordHash != null ? passwordHash : "";
    this.active = active;
  }

  public static UserPrincipal fromEntity(User user) {
    return new UserPrincipal(
        user.getId(),
        user.getOrganization().getId(),
        user.getEmail(),
        user.getRole(),
        user.getPasswordHash(),
        user.isActive());
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  @Override
  public String getPassword() {
    return passwordHash;
  }

  @Override
  public String getUsername() {
    return id.toString();
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return active;
  }
}
