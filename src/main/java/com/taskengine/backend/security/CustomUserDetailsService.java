package com.taskengine.backend.security;

import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.taskengine.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    if (username == null || username.isBlank()) {
      throw new UsernameNotFoundException("User not found");
    }
    try {
      UUID id = UUID.fromString(username);
      return userRepository
          .findByIdWithOrganization(id)
          .map(UserPrincipal::fromEntity)
          .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    } catch (IllegalArgumentException ex) {
      return userRepository
          .findByEmailWithOrganization(username)
          .map(UserPrincipal::fromEntity)
          .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
  }
}
