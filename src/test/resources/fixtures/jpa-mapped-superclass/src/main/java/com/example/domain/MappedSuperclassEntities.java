package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
class BaseEntity {
  @Id
  private Long id;
}

@Entity
class Owner extends BaseEntity {
}

class PlainBase {
  @Id
  private Long id;
}

@Entity
class PlainOwner extends PlainBase {
}

@Entity
class MissingOwner extends MissingBase {
}
