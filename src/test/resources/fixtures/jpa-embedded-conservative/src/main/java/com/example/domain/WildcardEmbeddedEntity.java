package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;

@Entity
class WildcardEmbeddedEntity {
  @Id
  private Long id;

  @Embedded
  private WildcardAddress address;
}
