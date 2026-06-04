package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
class SpoofedJpaEntity {
  @Id
  private Long id;

  @ManyToOne
  private SpoofedJpaEntity parent;
}
