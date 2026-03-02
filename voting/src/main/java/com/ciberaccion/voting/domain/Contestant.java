package com.ciberaccion.voting.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "contestant")
@Data
public class Contestant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "season_id", nullable = false)
  private Long seasonId;

  @Column(nullable = false, length = 120)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ContestantStatus status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}