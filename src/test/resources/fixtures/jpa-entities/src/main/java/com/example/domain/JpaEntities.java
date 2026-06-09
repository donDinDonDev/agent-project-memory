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

@Embeddable
class Address {
  @Column(name = "zip_code")
  private String zipCode;
}

@Embeddable
class ShipmentId {
  @Column(name = "tracking_number")
  private String trackingNumber;
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

  @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinColumn(
      name = "customer_id",
      referencedColumnName = "id",
      nullable = false,
      unique = true,
      insertable = false,
      updatable = true)
  private Customer customer;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<OrderLine> lines;

  @OneToOne(optional = false, fetch = FetchType.LAZY, orphanRemoval = true)
  @JoinColumn(name = "invoice_id")
  private Invoice invoice;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "order_tags",
      schema = "sales",
      catalog = "crm",
      joinColumns = @JoinColumn(name = "order_id", referencedColumnName = "id"),
      inverseJoinColumns = {
          @JoinColumn(name = "tag_id", referencedColumnName = "id", nullable = false)
      })
  private Set<Tag> tags;
}

@Entity
class Shipment {
  @EmbeddedId
  private ShipmentId id;

  @Embedded
  private Address destination;
}

@Entity
class ExternalProfile {
  @Id
  private Long id;

  @Embedded
  private ExternalAddress address;
}

@Entity
@IdClass(LegacyOrderKey.class)
class LegacyOrder {
  @Id
  private String tenantId;

  @Id
  private Long orderNumber;
}

class LegacyOrderKey {
  private String tenantId;
  private Long orderNumber;
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
