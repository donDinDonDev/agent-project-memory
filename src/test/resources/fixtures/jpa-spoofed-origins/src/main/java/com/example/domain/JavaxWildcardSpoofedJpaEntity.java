package com.example.domain;

import javax.persistence.*;

@Entity
class JavaxWildcardSpoofedJpaEntity {
  @Id
  private Long id;

  @ManyToOne
  private JavaxWildcardSpoofedJpaEntity parent;
}
