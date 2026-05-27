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
class ProjectCustomer {
  @Id
  private Long id;
}

@Entity
@Table(name = "orders")
class ProjectOrder {
  @Id
  private Long id;

  @ManyToOne
  private ProjectCustomer customer;

  @OneToMany
  private List<ProjectOrderLine> lines;

  @OneToOne
  private ProjectInvoice invoice;

  @ManyToMany
  private Set<ProjectTag> tags;
}
