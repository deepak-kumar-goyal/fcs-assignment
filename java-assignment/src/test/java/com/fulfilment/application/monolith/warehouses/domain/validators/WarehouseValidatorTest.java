package com.fulfilment.application.monolith.warehouses.domain.validators;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.usecases.InMemoryWarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WarehouseValidatorTest {

  private WarehouseValidator validator;

  @BeforeEach
  void setUp() {
    InMemoryWarehouseStore store = new InMemoryWarehouseStore();
    validator = new WarehouseValidator(store, new LocationGateway());
  }

  @Test
  void rejectsMissingWarehouseAndLocation() {
    assertKind(
        WarehouseBusinessException.Kind.NOT_FOUND,
        assertThrows(
            WarehouseBusinessException.class,
            () -> validator.validateForCreation(null)));

    Warehouse warehouse =
        InMemoryWarehouseStore.warehouse("MWH.100", " ", 10, 1);
    assertKind(
        WarehouseBusinessException.Kind.VALIDATION,
        assertThrows(
            WarehouseBusinessException.class,
            () -> validator.validateForCreation(warehouse)));
  }

  @Test
  void rejectsInvalidCapacityAndStockValues() {
    Warehouse nullCapacity =
        InMemoryWarehouseStore.warehouse("MWH.100", "VETSBY-001", 10, 1);
    nullCapacity.capacity = null;
    assertThrows(
        WarehouseBusinessException.class,
        () -> validator.validateForCreation(nullCapacity));

    Warehouse negativeStock =
        InMemoryWarehouseStore.warehouse("MWH.101", "VETSBY-001", 10, -1);
    assertThrows(
        WarehouseBusinessException.class,
        () -> validator.validateForCreation(negativeStock));

    Warehouse nullStock =
        InMemoryWarehouseStore.warehouse("MWH.102", "VETSBY-001", 10, 1);
    nullStock.stock = null;
    assertThrows(
        WarehouseBusinessException.class,
        () -> validator.validateForCreation(nullStock));
  }

  @Test
  void rejectsReplacementWithoutIdentity() {
    Warehouse missingCode =
        InMemoryWarehouseStore.warehouse(" ", "VETSBY-001", 10, 1);
    assertThrows(
        WarehouseBusinessException.class,
        () -> validator.validateForReplacement(missingCode));
  }

  private static void assertKind(
      WarehouseBusinessException.Kind expected, WarehouseBusinessException exception) {
    assertEquals(expected, exception.kind());
  }
}
