package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  @Inject
  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    if (warehouse == null) {
      throw WarehouseBusinessException.notFound("Warehouse does not exist");
    }
    if (warehouse.isArchived()) {
      throw WarehouseBusinessException.validation(
          "Warehouse " + warehouse.businessUnitCode + " is already archived");
    }

    warehouse.archivedAt = LocalDateTime.now();
    warehouseStore.update(warehouse);
  }
}
