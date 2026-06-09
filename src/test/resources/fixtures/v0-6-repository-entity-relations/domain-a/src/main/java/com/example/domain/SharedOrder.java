package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
class SharedOrder {
  @Id
  private Long id;
}
