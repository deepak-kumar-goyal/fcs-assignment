package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class AssociateFulfilmentUseCase {

  static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  static final int MAX_WAREHOUSES_PER_STORE = 3;
  static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  private final FulfilmentAssignmentRepository assignments;
  private final ProductRepository products;
  private final WarehouseStore warehouseStore;

  @Inject
  public AssociateFulfilmentUseCase(
      FulfilmentAssignmentRepository assignments,
      ProductRepository products,
      WarehouseStore warehouseStore) {
    this.assignments = assignments;
    this.products = products;
    this.warehouseStore = warehouseStore;
  }

  public FulfilmentAssignment associate(
      Store store, Long productId, String warehouseBusinessUnitCode) {
    if (store == null) {
      throw WarehouseBusinessException.notFound("Store does not exist");
    }
    if (productId == null) {
      throw WarehouseBusinessException.validation("Product id is required");
    }
    if (warehouseBusinessUnitCode == null || warehouseBusinessUnitCode.isBlank()) {
      throw WarehouseBusinessException.validation("Warehouse business unit code is required");
    }

    Product product = products.findById(productId);
    if (product == null) {
      throw WarehouseBusinessException.notFound("Product with id of " + productId + " does not exist");
    }

    Warehouse warehouse = warehouseStore.findByBusinessUnitCode(warehouseBusinessUnitCode);
    if (warehouse == null) {
      throw WarehouseBusinessException.notFound(
          "Active warehouse with business unit code "
              + warehouseBusinessUnitCode
              + " does not exist");
    }

    if (assignments.findAssignment(store.id, product.id, warehouse.businessUnitCode) != null) {
      throw WarehouseBusinessException.validation(
          "This product is already fulfilled from warehouse "
              + warehouse.businessUnitCode
              + " for this store");
    }

    List<FulfilmentAssignment> forProductAtStore =
        assignments.findByStoreAndProduct(store.id, product.id);
    long productWarehouses =
        forProductAtStore.stream()
            .map(assignment -> assignment.warehouseBusinessUnitCode)
            .distinct()
            .count();
    if (productWarehouses >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw WarehouseBusinessException.validation(
          "Product "
              + product.id
              + " can be fulfilled by at most "
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
              + " warehouses per store");
    }

    List<FulfilmentAssignment> forStore = assignments.findByStoreId(store.id);
    long storeWarehouses =
        forStore.stream().map(assignment -> assignment.warehouseBusinessUnitCode).distinct().count();
    boolean storeAlreadyUsesWarehouse =
        forStore.stream()
            .anyMatch(
                assignment -> warehouse.businessUnitCode.equals(assignment.warehouseBusinessUnitCode));
    if (!storeAlreadyUsesWarehouse && storeWarehouses >= MAX_WAREHOUSES_PER_STORE) {
      throw WarehouseBusinessException.validation(
          "Store " + store.id + " can be fulfilled by at most " + MAX_WAREHOUSES_PER_STORE + " warehouses");
    }

    long productTypesAtWarehouse =
        assignments.findByWarehouse(warehouse.businessUnitCode).stream()
            .map(assignment -> assignment.product.id)
            .distinct()
            .count();
    boolean warehouseAlreadyStoresProduct =
        assignments.findByWarehouse(warehouse.businessUnitCode).stream()
            .anyMatch(assignment -> assignment.product.id.equals(product.id));
    if (!warehouseAlreadyStoresProduct && productTypesAtWarehouse >= MAX_PRODUCTS_PER_WAREHOUSE) {
      throw WarehouseBusinessException.validation(
          "Warehouse "
              + warehouse.businessUnitCode
              + " can store at most "
              + MAX_PRODUCTS_PER_WAREHOUSE
              + " product types");
    }

    FulfilmentAssignment assignment = new FulfilmentAssignment();
    assignment.store = store;
    assignment.product = product;
    assignment.warehouseBusinessUnitCode = warehouse.businessUnitCode;
    assignments.persist(assignment);
    return assignment;
  }
}
