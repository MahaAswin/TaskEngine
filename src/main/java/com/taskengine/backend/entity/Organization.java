package com.taskengine.backend.entity;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
public class Organization {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true, length = 64)
  private String slug;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private OrganizationPlan plan = OrganizationPlan.FREE;

  @PrePersist
  void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }
}
