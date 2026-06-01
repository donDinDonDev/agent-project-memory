package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
class BaseEntity {
  @Id
  private Long id;
}

@MappedSuperclass
class NamedEntity extends BaseEntity {
}

@MappedSuperclass
class BrokenEntity extends MissingBase {
}

@Entity
class Owner extends BaseEntity {
}

@Entity
class NamedOwner extends NamedEntity {
}

@Entity
class BrokenOwner extends BrokenEntity {
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
