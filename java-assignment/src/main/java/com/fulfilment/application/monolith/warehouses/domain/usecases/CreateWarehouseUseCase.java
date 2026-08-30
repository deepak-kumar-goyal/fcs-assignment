package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw WarehouseBusinessException.validation("Business unit code is required");
    }
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw WarehouseBusinessException.validation("Warehouse location is required");
    }

    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw WarehouseBusinessException.validation(
          "Warehouse with business unit code "
              + warehouse.businessUnitCode
              + " already exists");
    }

    WarehouseCapacityRules.requireBasicCapacityAndStock(warehouse);

    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    WarehouseCapacityRules.requireLocationFeasibility(
        location, warehouse, warehouseStore, null);

    warehouseStore.create(warehouse);
  }
}
