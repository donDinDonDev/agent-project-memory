package com.example.domain;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "wildcard_orders")
class WildcardOrder {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "wildcard-id")
  private Long id;

  @Column(name = "status_code", nullable = false)
  @Enumerated(EnumType.STRING)
  private WildcardStatus status;

  @Version
  private long version;

  @Embedded
  private WildcardAddress address;

  @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(name = "customer_id", referencedColumnName = "id")
  private WildcardCustomer customer;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WildcardLine> lines;
}

@Embeddable
class WildcardAddress {
  @Column(name = "zip")
  private String zip;
}

@Entity
class WildcardCustomer {
  @Id
  private Long id;
}

class WildcardLine {
}

enum WildcardStatus {
  NEW
}
