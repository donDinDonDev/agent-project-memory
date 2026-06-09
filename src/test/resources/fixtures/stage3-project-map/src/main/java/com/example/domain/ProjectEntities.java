package com.example.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
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

@Embeddable
class ProjectAddress {
  @Column(name = "postal_code")
  private String postalCode;
}

@Embeddable
class ProjectShipmentId {
  @Column(name = "tracking_number")
  private String trackingNumber;
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

  @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(
      name = "customer_id",
      referencedColumnName = "id",
      nullable = false,
      unique = true,
      insertable = false,
      updatable = true)
  private ProjectCustomer customer;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<ProjectOrderLine> lines;

  @OneToOne(optional = false, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "invoice_id")
  private ProjectInvoice invoice;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "order_tags",
      schema = "sales",
      catalog = "crm",
      joinColumns = @JoinColumn(name = "order_id", referencedColumnName = "id"),
      inverseJoinColumns = {
          @JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false)
      })
  private Set<ProjectTag> tags;
}

@Entity
class ProjectShipment {
  @EmbeddedId
  private ProjectShipmentId id;

  @Embedded
  private ProjectAddress destination;

  @Embedded
  private ExternalProjectAddress externalAddress;
}

@Entity
@IdClass(ProjectLegacyOrderKey.class)
class ProjectLegacyOrder {
  @Id
  private String tenantId;

  @Id
  private Long orderNumber;
}

class ProjectLegacyOrderKey {
  private String tenantId;
  private Long orderNumber;
}

@Entity
@Table(name = "visits")
class ProjectVisit extends ProjectBaseEntity {
}

enum ProjectOrderStatus {
  NEW,
  PAID
}
