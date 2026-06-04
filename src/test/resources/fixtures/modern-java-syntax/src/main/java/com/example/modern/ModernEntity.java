package com.example.modern;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
class ModernEntity {
  @Id
  Long id;

  boolean hasText(Object value) {
    return value instanceof String text && !text.isBlank();
  }
}
