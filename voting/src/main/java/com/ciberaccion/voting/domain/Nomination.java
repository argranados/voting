package com.ciberaccion.voting.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(
  name = "nomination",
  uniqueConstraints = @UniqueConstraint(name = "uq_nomination", columnNames = {"round_id", "contestant_id"})
)
@Data
public class Nomination {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "round_id", nullable = false)
  private Long roundId;

  @Column(name = "contestant_id", nullable = false)
  private Long contestantId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}