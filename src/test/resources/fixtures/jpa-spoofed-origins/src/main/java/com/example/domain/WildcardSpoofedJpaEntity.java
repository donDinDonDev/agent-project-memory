package com.example.domain;

import jakarta.persistence.*;

@Entity
class WildcardSpoofedJpaEntity {
  @Id
  private Long id;

  @ManyToOne
  private WildcardSpoofedJpaEntity parent;
}
