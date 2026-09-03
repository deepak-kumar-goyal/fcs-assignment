package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.validators.WarehouseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  private InMemoryWarehouseStore store;
  private ReplaceWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    store = new InMemoryWarehouseStore();
    WarehouseValidator validator = new WarehouseValidator(store, new LocationGateway());
    CreateWarehouseUseCase create = new CreateWarehouseUseCase(store, validator);
    create.create(InMemoryWarehouseStore.warehouse("MWH.100", "ZWOLLE-002", 20, 8));
    useCase = new ReplaceWarehouseUseCase(store, validator);
  }

  @Test
  void archivesPreviousWarehouseAndCreatesReplacementWithSameBusinessUnit() {
    Warehouse replacement = InMemoryWarehouseStore.warehouse("MWH.100", "ZWOLLE-002", 25, 8);

    useCase.replace(replacement);

    Warehouse active = store.findByBusinessUnitCode("MWH.100");
    assertEquals("25", String.valueOf(active.capacity));
    assertNull(active.archivedAt);
    assertEquals(
        1,
        store.allIncludingArchived().stream().filter(Warehouse::isArchived).count());
  }

  @Test
  void rejectsWhenStockDoesNotMatch() {
    Warehouse replacement = InMemoryWarehouseStore.warehouse("MWH.100", "ZWOLLE-002", 25, 1);

    WarehouseBusinessException exception =
        assertThrows(WarehouseBusinessException.class, () -> useCase.replace(replacement));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
  }

  @Test
  void rejectsWhenCapacityCannotHoldCurrentStock() {
    Warehouse replacement = InMemoryWarehouseStore.warehouse("MWH.100", "ZWOLLE-002", 5, 8);

    WarehouseBusinessException exception =
        assertThrows(WarehouseBusinessException.class, () -> useCase.replace(replacement));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
  }

  @Test
  void rejectsUnknownBusinessUnit() {
    Warehouse replacement = InMemoryWarehouseStore.warehouse("MISSING", "ZWOLLE-002", 25, 8);

    WarehouseBusinessException exception =
        assertThrows(WarehouseBusinessException.class, () -> useCase.replace(replacement));

    assertEquals(WarehouseBusinessException.Kind.NOT_FOUND, exception.kind());
  }

  @Test
  void allowsReplacementAtAFullLocationByExcludingTheArchivedWarehouse() {
    CreateWarehouseUseCase create =
        new CreateWarehouseUseCase(
            store, new WarehouseValidator(store, new LocationGateway()));
    create.create(InMemoryWarehouseStore.warehouse("MWH.200", "HELMOND-001", 20, 4));

    Warehouse replacement = InMemoryWarehouseStore.warehouse("MWH.200", "HELMOND-001", 30, 4);
    useCase.replace(replacement);

    assertNotNull(store.findByBusinessUnitCode("MWH.200"));
    assertEquals(30, store.findByBusinessUnitCode("MWH.200").capacity);
  }
}
