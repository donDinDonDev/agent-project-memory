package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.List;
import java.util.Set;

@Entity
class Customer {
  @Id
  private Long id;
}

@Entity
@Table(name = "orders")
class Order {
  @Id
  private Long id;

  @ManyToOne
  private Customer customer;

  @OneToMany
  private List<OrderLine> lines;

  @OneToOne
  private Invoice invoice;

  @ManyToMany
  private Set<Tag> tags;
}

class NotAnEntity {
  @Id
  private Long id;
}
