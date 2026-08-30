package com.fulfilment.application.monolith.warehouses.domain.models;

import java.time.LocalDateTime;

public class Warehouse {

  public String id;

  // unique identifier among active warehouses
  public String businessUnitCode;

  public String location;

  public Integer capacity;

  public Integer stock;

  public LocalDateTime createdAt;

  public LocalDateTime archivedAt;

  public boolean isArchived() {
    return archivedAt != null;
  }
}
