package com.ciberaccion.voting.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "round")
@Data
public class Round {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "season_id", nullable = false)
  private Long seasonId;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(name = "starts_at", nullable = false)
  private Instant startsAt;

  @Column(name = "ends_at", nullable = false)
  private Instant endsAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private RoundStatus status;

  @Column(name = "rule_type", nullable = false, length = 40)
  private String ruleType;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}