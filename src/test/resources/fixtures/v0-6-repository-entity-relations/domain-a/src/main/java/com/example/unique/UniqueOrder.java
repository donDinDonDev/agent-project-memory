package com.example.unique;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
class UniqueOrder {
  @Id
  private Long id;
}
