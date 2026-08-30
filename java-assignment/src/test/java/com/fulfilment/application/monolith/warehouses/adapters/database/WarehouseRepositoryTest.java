package com.fulfilment.application.monolith.warehouses.adapters.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseRepositoryTest {

  @Inject WarehouseRepository warehouseRepository;

  @Test
  @Transactional
  public void createUpdateAndRemoveWarehouse() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "MWH.REPO";
    warehouse.location = "EINDHOVEN-001";
    warehouse.capacity = 15;
    warehouse.stock = 2;

    warehouseRepository.create(warehouse);
    Warehouse stored = warehouseRepository.findByBusinessUnitCode("MWH.REPO");
    assertEquals(15, stored.capacity);

    stored.capacity = 18;
    warehouseRepository.update(stored);
    assertEquals(18, warehouseRepository.findByBusinessUnitCode("MWH.REPO").capacity);

    warehouseRepository.remove(stored);
    assertNull(warehouseRepository.findByBusinessUnitCode("MWH.REPO"));
    assertNull(warehouseRepository.getById(Long.valueOf(warehouse.id)));
  }
}
