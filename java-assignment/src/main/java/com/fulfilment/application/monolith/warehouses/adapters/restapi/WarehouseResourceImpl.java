package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject WarehouseStore warehouseStore;
  @Inject CreateWarehouseOperation createWarehouseOperation;
  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseStore.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    try {
      var domain = toDomain(data);
      createWarehouseOperation.create(domain);
      return toWarehouseResponse(domain);
    } catch (WarehouseBusinessException e) {
      throw toHttp(e);
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var warehouse = warehouseStore.getById(parseId(id));
    if (warehouse == null) {
      throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
    }
    return toWarehouseResponse(warehouse);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {
    try {
      var warehouse = warehouseStore.getById(parseId(id));
      if (warehouse == null) {
        throw WarehouseBusinessException.notFound("Warehouse with id of " + id + " does not exist.");
      }
      archiveWarehouseOperation.archive(warehouse);
    } catch (WarehouseBusinessException e) {
      throw toHttp(e);
    }
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    try {
      var domain = toDomain(data);
      domain.businessUnitCode = businessUnitCode;
      replaceWarehouseOperation.replace(domain);
      return toWarehouseResponse(domain);
    } catch (WarehouseBusinessException e) {
      throw toHttp(e);
    }
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setId(warehouse.id);
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);
    return response;
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(
      Warehouse data) {
    var warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.id = data.getId();
    warehouse.businessUnitCode = data.getBusinessUnitCode();
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();
    return warehouse;
  }

  private static Long parseId(String id) {
    try {
      return Long.parseLong(id);
    } catch (NumberFormatException e) {
      throw new WebApplicationException("Warehouse with id of " + id + " does not exist.", 404);
    }
  }

  private static WebApplicationException toHttp(WarehouseBusinessException exception) {
    int status =
        exception.kind() == WarehouseBusinessException.Kind.NOT_FOUND ? 404 : 400;
    return new WebApplicationException(exception.getMessage(), status);
  }
}
