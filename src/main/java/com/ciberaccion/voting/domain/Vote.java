package com.ciberaccion.voting.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "vote")
@Data
public class Vote {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "round_id", nullable = false)
  private Long roundId;

  @Column(name = "contestant_id", nullable = false)
  private Long contestantId;

  @Column(name = "voter_id")
  private Long voterId; // Nivel 1: null

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private VoteStatus status;

  @Column(name = "reject_reason", length = 200)
  private String rejectReason;
}