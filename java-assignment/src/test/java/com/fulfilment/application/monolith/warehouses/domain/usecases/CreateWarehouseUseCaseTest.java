package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.validators.WarehouseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

  private InMemoryWarehouseStore store;
  private CreateWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    store = new InMemoryWarehouseStore();
    useCase =
        new CreateWarehouseUseCase(
            store, new WarehouseValidator(store, new LocationGateway()));
  }

  @Test
  void createsWarehouseWhenLocationHasCapacity() {
    useCase.create(InMemoryWarehouseStore.warehouse("MWH.100", "ZWOLLE-002", 20, 5));

    assertEquals(1, store.getAll().size());
    assertEquals("MWH.100", store.findByBusinessUnitCode("MWH.100").businessUnitCode);
  }

  @Test
  void rejectsDuplicateBusinessUnitCode() {
    useCase.create(InMemoryWarehouseStore.warehouse("MWH.100", "ZWOLLE-002", 20, 5));

    WarehouseBusinessException exception =
        assertThrows(
            WarehouseBusinessException.class,
            () -> useCase.create(InMemoryWarehouseStore.warehouse("MWH.100", "EINDHOVEN-001", 10, 1)));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
  }

  @Test
  void rejectsUnknownLocation() {
    WarehouseBusinessException exception =
        assertThrows(
            WarehouseBusinessException.class,
            () -> useCase.create(InMemoryWarehouseStore.warehouse("MWH.100", "NOWHERE-001", 10, 1)));

    assertEquals(WarehouseBusinessException.Kind.NOT_FOUND, exception.kind());
  }

  @Test
  void rejectsWhenLocationWarehouseLimitReached() {
    useCase.create(InMemoryWarehouseStore.warehouse("MWH.100", "HELMOND-001", 20, 5));

    WarehouseBusinessException exception =
        assertThrows(
            WarehouseBusinessException.class,
            () -> useCase.create(InMemoryWarehouseStore.warehouse("MWH.101", "HELMOND-001", 10, 1)));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
  }

  @Test
  void rejectsWhenLocationCapacityWouldBeExceeded() {
    useCase.create(InMemoryWarehouseStore.warehouse("MWH.100", "ZWOLLE-002", 40, 5));

    WarehouseBusinessException exception =
        assertThrows(
            WarehouseBusinessException.class,
            () -> useCase.create(InMemoryWarehouseStore.warehouse("MWH.101", "ZWOLLE-002", 20, 1)));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
  }

  @Test
  void rejectsStockGreaterThanCapacity() {
    WarehouseBusinessException exception =
        assertThrows(
            WarehouseBusinessException.class,
            () -> useCase.create(InMemoryWarehouseStore.warehouse("MWH.100", "VETSBY-001", 10, 20)));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
  }

  @Test
  void rejectsMissingBusinessUnitCode() {
    WarehouseBusinessException exception =
        assertThrows(
            WarehouseBusinessException.class,
            () -> useCase.create(InMemoryWarehouseStore.warehouse(" ", "VETSBY-001", 10, 1)));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
    assertNotNull(exception.getMessage());
  }
}
