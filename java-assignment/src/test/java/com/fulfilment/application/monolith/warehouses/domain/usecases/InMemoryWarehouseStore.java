package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryWarehouseStore implements WarehouseStore {

  private final List<Warehouse> warehouses = new ArrayList<>();
  private final AtomicLong ids = new AtomicLong(1);

  @Override
  public List<Warehouse> getAll() {
    return warehouses.stream().filter(warehouse -> warehouse.archivedAt == null).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    warehouse.id = String.valueOf(ids.getAndIncrement());
    warehouses.add(copy(warehouse));
  }

  @Override
  public void update(Warehouse warehouse) {
    for (int i = 0; i < warehouses.size(); i++) {
      if (warehouses.get(i).id.equals(warehouse.id)) {
        warehouses.set(i, copy(warehouse));
        return;
      }
    }
  }

  @Override
  public void remove(Warehouse warehouse) {
    warehouses.removeIf(existing -> existing.id.equals(warehouse.id));
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    return warehouses.stream()
        .filter(warehouse -> warehouse.archivedAt == null)
        .filter(warehouse -> buCode.equals(warehouse.businessUnitCode))
        .findFirst()
        .map(InMemoryWarehouseStore::copy)
        .orElse(null);
  }

  @Override
  public Warehouse getById(Long id) {
    return warehouses.stream()
        .filter(warehouse -> String.valueOf(id).equals(warehouse.id))
        .findFirst()
        .map(InMemoryWarehouseStore::copy)
        .orElse(null);
  }

  public List<Warehouse> allIncludingArchived() {
    return warehouses.stream().map(InMemoryWarehouseStore::copy).toList();
  }

  public static Warehouse warehouse(String buCode, String location, int capacity, int stock) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = buCode;
    warehouse.location = location;
    warehouse.capacity = capacity;
    warehouse.stock = stock;
    return warehouse;
  }

  private static Warehouse copy(Warehouse source) {
    Warehouse copy = new Warehouse();
    copy.id = source.id;
    copy.businessUnitCode = source.businessUnitCode;
    copy.location = source.location;
    copy.capacity = source.capacity;
    copy.stock = source.stock;
    copy.createdAt = source.createdAt;
    copy.archivedAt = source.archivedAt;
    return copy;
  }
}
