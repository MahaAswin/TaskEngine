package com.taskengine.backend.entity;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "team_audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class TeamAuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "team_id", nullable = false)
  private Team team;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "actor_id", nullable = false)
  private User actor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private TeamAuditAction action;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "affected_user_id")
  private User affectedUser;

  @Column(nullable = false)
  private Instant timestamp = Instant.now();
}
