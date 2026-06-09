package com.example.domain;

import jakarta.persistence.*;

@interface Entity {
}

@interface Id {
}

@Entity
class LocalFakeWildcardEntity {
  @Id
  private Long id;
}
