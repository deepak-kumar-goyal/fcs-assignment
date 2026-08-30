package com.fulfilment.application.monolith.fulfilment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class FulfilmentAssignmentRepository implements PanacheRepository<FulfilmentAssignment> {

  public List<FulfilmentAssignment> findByStoreId(Long storeId) {
    return list("store.id", storeId);
  }

  public List<FulfilmentAssignment> findByStoreAndProduct(Long storeId, Long productId) {
    return list("store.id = ?1 and product.id = ?2", storeId, productId);
  }

  public List<FulfilmentAssignment> findByWarehouse(String warehouseBusinessUnitCode) {
    return list("warehouseBusinessUnitCode", warehouseBusinessUnitCode);
  }

  public FulfilmentAssignment findAssignment(Long storeId, Long productId, String warehouseBuCode) {
    return find(
            "store.id = ?1 and product.id = ?2 and warehouseBusinessUnitCode = ?3",
            storeId,
            productId,
            warehouseBuCode)
        .firstResult();
  }
}
