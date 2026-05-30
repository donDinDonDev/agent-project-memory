package com.example.modern;

@Entity
class ModernEntity {
  @Id
  Long id;

  boolean hasText(Object value) {
    return value instanceof String text && !text.isBlank();
  }
}
