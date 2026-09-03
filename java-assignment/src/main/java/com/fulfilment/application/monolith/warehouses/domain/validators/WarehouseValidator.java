package com.fulfilment.application.monolith.warehouses.domain.validators;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class WarehouseValidator {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  @Inject
  public WarehouseValidator(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  public void validateForCreation(Warehouse warehouse) {
    requireWarehouse(warehouse);
    requireIdentity(warehouse);
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw WarehouseBusinessException.validation(
          "Warehouse with business unit code " + warehouse.businessUnitCode + " already exists");
    }

    requireBasicCapacityAndStock(warehouse);
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    requireLocationFeasibility(location, warehouse, null);
  }

  public Warehouse validateForReplacement(Warehouse replacement) {
    requireWarehouse(replacement);
    requireIdentity(replacement);

    Warehouse current =
        warehouseStore.findByBusinessUnitCode(replacement.businessUnitCode);
    if (current == null) {
      throw WarehouseBusinessException.notFound(
          "Active warehouse with business unit code "
              + replacement.businessUnitCode
              + " does not exist");
    }

    requireBasicCapacityAndStock(replacement);
    if (!Objects.equals(replacement.stock, current.stock)) {
      throw WarehouseBusinessException.validation(
          "Replacement warehouse stock must match the current stock of " + current.stock);
    }
    if (replacement.capacity < current.stock) {
      throw WarehouseBusinessException.validation(
          "Replacement warehouse capacity "
              + replacement.capacity
              + " cannot accommodate current stock of "
              + current.stock);
    }

    Location location = locationResolver.resolveByIdentifier(replacement.location);
    requireLocationFeasibility(location, replacement, current);
    return current;
  }

  public void validateForArchive(Warehouse warehouse) {
    requireWarehouse(warehouse);
    if (warehouse.isArchived()) {
      throw WarehouseBusinessException.validation(
          "Warehouse " + warehouse.businessUnitCode + " is already archived");
    }
  }

  private static void requireWarehouse(Warehouse warehouse) {
    if (warehouse == null) {
      throw WarehouseBusinessException.notFound("Warehouse does not exist");
    }
  }

  private static void requireIdentity(Warehouse warehouse) {
    if (warehouse.businessUnitCode == null || warehouse.businessUnitCode.isBlank()) {
      throw WarehouseBusinessException.validation("Business unit code is required");
    }
    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw WarehouseBusinessException.validation("Warehouse location is required");
    }
  }

  private static void requireBasicCapacityAndStock(Warehouse warehouse) {
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

  private void requireLocationFeasibility(
      Location location, Warehouse incoming, Warehouse excluding) {
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
