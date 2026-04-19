package com.taskengine.backend.entity;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "org_id", nullable = false)
  private Organization organization;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "password_hash")
  private String passwordHash;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private UserRole role;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(name = "google_sub", unique = true)
  private String googleSub;

  @PrePersist
  void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
