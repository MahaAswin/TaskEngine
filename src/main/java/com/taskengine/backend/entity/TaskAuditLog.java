package com.taskengine.backend.entity;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "task_audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class TaskAuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "task_id", nullable = false)
  private Task task;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "actor_id", nullable = false)
  private User actor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private TaskAuditAction action;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "old_value")
  private Map<String, Object> oldValue;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "new_value")
  private Map<String, Object> newValue;

  @Column(nullable = false)
  private Instant timestamp = Instant.now();
}
