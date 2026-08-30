package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;
import java.util.Objects;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public ReplaceWarehouseUseCase(
      WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse.businessUnitCode == null || newWarehouse.businessUnitCode.isBlank()) {
      throw WarehouseBusinessException.validation("Business unit code is required");
    }
    if (newWarehouse.location == null || newWarehouse.location.isBlank()) {
      throw WarehouseBusinessException.validation("Warehouse location is required");
    }

    Warehouse current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null) {
      throw WarehouseBusinessException.notFound(
          "Active warehouse with business unit code "
              + newWarehouse.businessUnitCode
              + " does not exist");
    }

    WarehouseCapacityRules.requireBasicCapacityAndStock(newWarehouse);

    if (!Objects.equals(newWarehouse.stock, current.stock)) {
      throw WarehouseBusinessException.validation(
          "Replacement warehouse stock must match the current stock of " + current.stock);
    }
    if (newWarehouse.capacity < current.stock) {
      throw WarehouseBusinessException.validation(
          "Replacement warehouse capacity "
              + newWarehouse.capacity
              + " cannot accommodate current stock of "
              + current.stock);
    }

    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    WarehouseCapacityRules.requireLocationFeasibility(
        location, newWarehouse, warehouseStore, current);

    current.archivedAt = LocalDateTime.now();
    warehouseStore.update(current);

    newWarehouse.businessUnitCode = current.businessUnitCode;
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
