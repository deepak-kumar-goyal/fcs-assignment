package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.List;
import java.util.Objects;

final class WarehouseCapacityRules {

  private WarehouseCapacityRules() {}

  static void requireBasicCapacityAndStock(Warehouse warehouse) {
    if (warehouse.capacity == null || warehouse.capacity < 0) {
      throw WarehouseBusinessException.validation("Warehouse capacity must be zero or greater");
    }
    if (warehouse.stock == null || warehouse.stock < 0) {
      throw WarehouseBusinessException.validation("Warehouse stock must be zero or greater");
    }
    if (warehouse.stock > warehouse.capacity) {
      throw WarehouseBusinessException.validation(
          "Warehouse stock " + warehouse.stock + " exceeds capacity " + warehouse.capacity);
    }
  }

  static void requireLocationFeasibility(
      Location location, Warehouse incoming, WarehouseStore warehouseStore, Warehouse excluding) {
    List<Warehouse> others =
        warehouseStore.getAll().stream()
            .filter(existing -> location.identification.equals(existing.location))
            .filter(existing -> excluding == null || !sameWarehouse(existing, excluding))
            .toList();

    if (others.size() >= location.maxNumberOfWarehouses) {
      throw WarehouseBusinessException.validation(
          "Location "
              + location.identification
              + " already has the maximum number of warehouses ("
              + location.maxNumberOfWarehouses
              + ")");
    }

    int usedCapacity = others.stream().mapToInt(warehouse -> warehouse.capacity).sum();
    if (usedCapacity + incoming.capacity > location.maxCapacity) {
      throw WarehouseBusinessException.validation(
          "Warehouse capacity "
              + incoming.capacity
              + " would exceed remaining location capacity of "
              + (location.maxCapacity - usedCapacity)
              + " at "
              + location.identification);
    }
  }

  private static boolean sameWarehouse(Warehouse left, Warehouse right) {
    if (left.id != null && right.id != null) {
      return left.id.equals(right.id);
    }
    return Objects.equals(left.businessUnitCode, right.businessUnitCode);
  }
}
