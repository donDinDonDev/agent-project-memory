package com.example.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
class Customer {
  @Id
  Long id;
}

@Entity
class Order {
  @Id
  Long id;

  @ManyToOne
  Customer customer;
}
