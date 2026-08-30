package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return find("archivedAt is null").list().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    DbWarehouse entity = new DbWarehouse();
    if (warehouse.createdAt == null) {
      warehouse.createdAt = LocalDateTime.now();
    }
    entity.apply(warehouse);
    persist(entity);
    warehouse.id = String.valueOf(entity.id);
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse entity = resolveEntity(warehouse);
    if (entity == null) {
      return;
    }
    entity.apply(warehouse);
    warehouse.id = String.valueOf(entity.id);
  }

  @Override
  public void remove(Warehouse warehouse) {
    DbWarehouse entity = resolveEntity(warehouse);
    if (entity != null) {
      delete(entity);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse entity = find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
    return entity == null ? null : entity.toWarehouse();
  }

  @Override
  public Warehouse getById(Long id) {
    DbWarehouse entity = findByIdOptional(id).orElse(null);
    return entity == null ? null : entity.toWarehouse();
  }

  private DbWarehouse resolveEntity(Warehouse warehouse) {
    if (warehouse.id != null && !warehouse.id.isBlank()) {
      try {
        return findByIdOptional(Long.parseLong(warehouse.id)).orElse(null);
      } catch (NumberFormatException ignored) {
        // fall through to business unit lookup
      }
    }
    if (warehouse.businessUnitCode == null) {
      return null;
    }
    DbWarehouse active =
        find("businessUnitCode = ?1 and archivedAt is null", warehouse.businessUnitCode)
            .firstResult();
    if (active != null) {
      return active;
    }
    return find("businessUnitCode", warehouse.businessUnitCode).firstResult();
  }
}
