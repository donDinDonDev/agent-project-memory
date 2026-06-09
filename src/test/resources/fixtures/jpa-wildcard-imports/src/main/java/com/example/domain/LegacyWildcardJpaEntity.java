package com.example.domain;

import javax.persistence.*;

@Entity
class LegacyWildcardEntity {
  @Id
  @Column(name = "legacy_id")
  private Long id;
}
