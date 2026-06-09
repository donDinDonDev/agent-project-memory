package com.example.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "order-id")
  private Long id;

  @Column(
      name = "status_code",
      nullable = false,
      unique = true,
      length = 24,
      precision = 10,
      scale = 2,
      insertable = false,
      updatable = true)
  @Enumerated(EnumType.STRING)
  private OrderStatus status;

  @Version
  private long version;

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

@Entity
class PropertyAccessEntity {
  @Id
  private Long id;

  private String code;

  @Column(name = "code")
  String getCode() {
    return code;
  }
}

enum OrderStatus {
  NEW,
  PAID
}
