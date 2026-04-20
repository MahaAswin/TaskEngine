package com.taskengine.backend.entity;

import java.time.Instant;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "team_members")
@Getter
@Setter
@NoArgsConstructor
public class TeamMember {

  @EmbeddedId
  private TeamMemberId id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("teamId")
  @JoinColumn(name = "team_id", nullable = false)
  private Team team;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @MapsId("userId")
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private TeamMemberRole role = TeamMemberRole.MEMBER;

  @Column(name = "joined_at", nullable = false, updatable = false)
  private Instant joinedAt;

  @PrePersist
  void prePersist() {
    if (joinedAt == null) {
      joinedAt = Instant.now();
    }
  }
}
