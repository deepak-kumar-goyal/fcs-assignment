package com.fulfilment.application.monolith.fulfilment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.usecases.InMemoryWarehouseStore;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AssociateFulfilmentUseCaseTest {

  private InMemoryFulfilmentAssignmentRepository assignments;
  private InMemoryProductRepository products;
  private InMemoryWarehouseStore warehouses;
  private AssociateFulfilmentUseCase useCase;
  private Store store;

  @BeforeEach
  void setUp() {
    assignments = new InMemoryFulfilmentAssignmentRepository();
    products = new InMemoryProductRepository();
    warehouses = new InMemoryWarehouseStore();
    products.add(product(1L, "TONSTAD"));
    products.add(product(2L, "KALLAX"));
    products.add(product(3L, "BESTA"));
    products.add(product(4L, "MALM"));
    products.add(product(5L, "KIVIK"));
    products.add(product(6L, "HEMNES"));
    warehouses.create(InMemoryWarehouseStore.warehouse("WH.1", "ZWOLLE-002", 20, 1));
    warehouses.create(InMemoryWarehouseStore.warehouse("WH.2", "ZWOLLE-002", 20, 1));
    warehouses.create(InMemoryWarehouseStore.warehouse("WH.3", "EINDHOVEN-001", 20, 1));
    warehouses.create(InMemoryWarehouseStore.warehouse("WH.4", "EINDHOVEN-001", 20, 1));
    store = new Store("City Store");
    store.id = 10L;
    useCase = new AssociateFulfilmentUseCase(assignments, products, warehouses);
  }

  @Test
  void associatesProductStoreAndWarehouse() {
    FulfilmentAssignment assignment = useCase.associate(store, 1L, "WH.1");

    assertEquals("WH.1", assignment.warehouseBusinessUnitCode);
    assertEquals(1L, assignment.product.id);
  }

  @Test
  void rejectsMoreThanTwoWarehousesForTheSameProductAtAStore() {
    useCase.associate(store, 1L, "WH.1");
    useCase.associate(store, 1L, "WH.2");

    WarehouseBusinessException exception =
        assertThrows(WarehouseBusinessException.class, () -> useCase.associate(store, 1L, "WH.3"));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
  }

  @Test
  void rejectsMoreThanThreeWarehousesForAStore() {
    useCase.associate(store, 1L, "WH.1");
    useCase.associate(store, 2L, "WH.2");
    useCase.associate(store, 3L, "WH.3");

    WarehouseBusinessException exception =
        assertThrows(WarehouseBusinessException.class, () -> useCase.associate(store, 4L, "WH.4"));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
  }

  @Test
  void rejectsMoreThanFiveProductTypesOnAWarehouse() {
    Store other = new Store("Other");
    other.id = 11L;
    useCase.associate(store, 1L, "WH.1");
    useCase.associate(other, 2L, "WH.1");
    useCase.associate(other, 3L, "WH.1");
    useCase.associate(other, 4L, "WH.1");
    useCase.associate(other, 5L, "WH.1");

    WarehouseBusinessException exception =
        assertThrows(WarehouseBusinessException.class, () -> useCase.associate(other, 6L, "WH.1"));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
  }

  @Test
  void rejectsUnknownWarehouse() {
    WarehouseBusinessException exception =
        assertThrows(WarehouseBusinessException.class, () -> useCase.associate(store, 1L, "MISSING"));

    assertEquals(WarehouseBusinessException.Kind.NOT_FOUND, exception.kind());
  }

  private static Product product(Long id, String name) {
    Product product = new Product(name);
    product.id = id;
    return product;
  }

  static class InMemoryProductRepository extends ProductRepository {
    private final List<Product> items = new ArrayList<>();

    void add(Product product) {
      items.add(product);
    }

    @Override
    public Product findById(Long id) {
      return items.stream().filter(product -> id.equals(product.id)).findFirst().orElse(null);
    }
  }

  static class InMemoryFulfilmentAssignmentRepository extends FulfilmentAssignmentRepository {
    private final List<FulfilmentAssignment> items = new ArrayList<>();
    private final AtomicLong ids = new AtomicLong(1);

    @Override
    public void persist(FulfilmentAssignment assignment) {
      assignment.id = ids.getAndIncrement();
      items.add(assignment);
    }

    @Override
    public List<FulfilmentAssignment> findByStoreId(Long storeId) {
      return items.stream().filter(item -> item.store.id.equals(storeId)).toList();
    }

    @Override
    public List<FulfilmentAssignment> findByStoreAndProduct(Long storeId, Long productId) {
      return items.stream()
          .filter(item -> item.store.id.equals(storeId) && item.product.id.equals(productId))
          .toList();
    }

    @Override
    public List<FulfilmentAssignment> findByWarehouse(String warehouseBusinessUnitCode) {
      return items.stream()
          .filter(item -> warehouseBusinessUnitCode.equals(item.warehouseBusinessUnitCode))
          .toList();
    }

    @Override
    public FulfilmentAssignment findAssignment(
        Long storeId, Long productId, String warehouseBuCode) {
      return items.stream()
          .filter(
              item ->
                  item.store.id.equals(storeId)
                      && item.product.id.equals(productId)
                      && warehouseBuCode.equals(item.warehouseBusinessUnitCode))
          .findFirst()
          .orElse(null);
    }
  }
}
