package com.fulfilment.application.monolith.fulfilment.domain.model;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "fulfilment_assignment",
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"store_id", "product_id", "warehouseBusinessUnitCode"}))
public class FulfilmentAssignment {

  @Id @GeneratedValue public Long id;

  @ManyToOne(optional = false)
  public Store store;

  @ManyToOne(optional = false)
  public Product product;

  public String warehouseBusinessUnitCode;
}
