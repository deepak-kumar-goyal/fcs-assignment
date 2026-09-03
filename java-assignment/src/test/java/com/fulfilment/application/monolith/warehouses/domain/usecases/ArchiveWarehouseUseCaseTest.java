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

public class ArchiveWarehouseUseCaseTest {

  private InMemoryWarehouseStore store;
  private ArchiveWarehouseUseCase useCase;

  @BeforeEach
  void setUp() {
    store = new InMemoryWarehouseStore();
    WarehouseValidator validator = new WarehouseValidator(store, new LocationGateway());
    new CreateWarehouseUseCase(store, validator)
        .create(InMemoryWarehouseStore.warehouse("MWH.100", "VETSBY-001", 40, 4));
    useCase = new ArchiveWarehouseUseCase(store, validator);
  }

  @Test
  void archivesActiveWarehouse() {
    Warehouse warehouse = store.findByBusinessUnitCode("MWH.100");

    useCase.archive(warehouse);

    assertNull(store.findByBusinessUnitCode("MWH.100"));
    assertNotNull(store.getById(Long.valueOf(warehouse.id)).archivedAt);
  }

  @Test
  void rejectsMissingWarehouse() {
    WarehouseBusinessException exception =
        assertThrows(WarehouseBusinessException.class, () -> useCase.archive(null));

    assertEquals(WarehouseBusinessException.Kind.NOT_FOUND, exception.kind());
  }

  @Test
  void rejectsAlreadyArchivedWarehouse() {
    Warehouse warehouse = store.findByBusinessUnitCode("MWH.100");
    useCase.archive(warehouse);

    Warehouse archived = store.getById(Long.valueOf(warehouse.id));
    WarehouseBusinessException exception =
        assertThrows(WarehouseBusinessException.class, () -> useCase.archive(archived));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
  }
}
