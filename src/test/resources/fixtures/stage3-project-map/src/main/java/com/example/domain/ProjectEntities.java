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
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.List;
import java.util.Set;

@MappedSuperclass
class ProjectBaseEntity {
  @Id
  private Long id;
}

@Entity
class ProjectCustomer {
  @Id
  private Long id;
}

@Entity
@Table(name = "orders")
class ProjectOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "status", nullable = false, length = 32)
  @Enumerated(EnumType.STRING)
  private ProjectOrderStatus status;

  @Version
  private long version;

  @ManyToOne
  private ProjectCustomer customer;

  @OneToMany
  private List<ProjectOrderLine> lines;

  @OneToOne
  private ProjectInvoice invoice;

  @ManyToMany
  private Set<ProjectTag> tags;
}

@Entity
@Table(name = "visits")
class ProjectVisit extends ProjectBaseEntity {
}

enum ProjectOrderStatus {
  NEW,
  PAID
}
