package com.fulfilment.application.monolith.fulfilment.domain.validator;

import com.fulfilment.application.monolith.fulfilment.adapters.database.FulfilmentAssignmentRepository;
import com.fulfilment.application.monolith.fulfilment.domain.FulfilmentBusinessException;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssignment;
import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class FulfilmentValidator {

  static final int MAX_WAREHOUSES_PER_PRODUCT_PER_STORE = 2;
  static final int MAX_WAREHOUSES_PER_STORE = 3;
  static final int MAX_PRODUCTS_PER_WAREHOUSE = 5;

  private final FulfilmentAssignmentRepository assignments;
  private final ProductRepository products;
  private final WarehouseStore warehouses;

  @Inject
  public FulfilmentValidator(
      FulfilmentAssignmentRepository assignments,
      ProductRepository products,
      WarehouseStore warehouses) {
    this.assignments = assignments;
    this.products = products;
    this.warehouses = warehouses;
  }

  public ValidatedAssociation validate(
      Store store, Long productId, String warehouseBusinessUnitCode) {
    requireInput(store, productId, warehouseBusinessUnitCode);

    Product product = products.findById(productId);
    if (product == null) {
      throw FulfilmentBusinessException.notFound(
          "Product with id of " + productId + " does not exist");
    }

    Warehouse warehouse = warehouses.findByBusinessUnitCode(warehouseBusinessUnitCode);
    if (warehouse == null) {
      throw FulfilmentBusinessException.notFound(
          "Active warehouse with business unit code "
              + warehouseBusinessUnitCode
              + " does not exist");
    }

    requireUniqueAssociation(store, product, warehouse);
    requireProductWarehouseLimit(store, product);
    requireStoreWarehouseLimit(store, warehouse);
    requireWarehouseProductLimit(product, warehouse);
    return new ValidatedAssociation(product, warehouse);
  }

  private static void requireInput(
      Store store, Long productId, String warehouseBusinessUnitCode) {
    if (store == null) {
      throw FulfilmentBusinessException.notFound("Store does not exist");
    }
    if (productId == null) {
      throw FulfilmentBusinessException.validation("Product id is required");
    }
    if (warehouseBusinessUnitCode == null || warehouseBusinessUnitCode.isBlank()) {
      throw FulfilmentBusinessException.validation(
          "Warehouse business unit code is required");
    }
  }

  private void requireUniqueAssociation(Store store, Product product, Warehouse warehouse) {
    if (assignments.findAssignment(store.id, product.id, warehouse.businessUnitCode) != null) {
      throw FulfilmentBusinessException.validation(
          "This product is already fulfilled from warehouse "
              + warehouse.businessUnitCode
              + " for this store");
    }
  }

  private void requireProductWarehouseLimit(Store store, Product product) {
    long warehouseCount =
        assignments.findByStoreAndProduct(store.id, product.id).stream()
            .map(assignment -> assignment.warehouseBusinessUnitCode)
            .distinct()
            .count();
    if (warehouseCount >= MAX_WAREHOUSES_PER_PRODUCT_PER_STORE) {
      throw FulfilmentBusinessException.validation(
          "Product "
              + product.id
              + " can be fulfilled by at most "
              + MAX_WAREHOUSES_PER_PRODUCT_PER_STORE
              + " warehouses per store");
    }
  }

  private void requireStoreWarehouseLimit(Store store, Warehouse warehouse) {
    List<FulfilmentAssignment> storeAssignments = assignments.findByStoreId(store.id);
    boolean alreadyUsed =
        storeAssignments.stream()
            .anyMatch(
                assignment ->
                    warehouse.businessUnitCode.equals(assignment.warehouseBusinessUnitCode));
    long warehouseCount =
        storeAssignments.stream()
            .map(assignment -> assignment.warehouseBusinessUnitCode)
            .distinct()
            .count();
    if (!alreadyUsed && warehouseCount >= MAX_WAREHOUSES_PER_STORE) {
      throw FulfilmentBusinessException.validation(
          "Store "
              + store.id
              + " can be fulfilled by at most "
              + MAX_WAREHOUSES_PER_STORE
              + " warehouses");
    }
  }

  private void requireWarehouseProductLimit(Product product, Warehouse warehouse) {
    List<FulfilmentAssignment> warehouseAssignments =
        assignments.findByWarehouse(warehouse.businessUnitCode);
    boolean alreadyStored =
        warehouseAssignments.stream()
            .anyMatch(assignment -> assignment.product.id.equals(product.id));
    long productCount =
        warehouseAssignments.stream()
            .map(assignment -> assignment.product.id)
            .distinct()
            .count();
    if (!alreadyStored && productCount >= MAX_PRODUCTS_PER_WAREHOUSE) {
      throw FulfilmentBusinessException.validation(
          "Warehouse "
              + warehouse.businessUnitCode
              + " can store at most "
              + MAX_PRODUCTS_PER_WAREHOUSE
              + " product types");
    }
  }

  public record ValidatedAssociation(Product product, Warehouse warehouse) {}
}
