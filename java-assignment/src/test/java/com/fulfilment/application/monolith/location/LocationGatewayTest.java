package com.fulfilment.application.monolith.location;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

public class LocationGatewayTest {

  private final LocationGateway locationGateway = new LocationGateway();

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testWhenResolveUnknownLocationShouldFail() {
    WarehouseBusinessException exception =
        assertThrows(
            WarehouseBusinessException.class,
            () -> locationGateway.resolveByIdentifier("UNKNOWN-001"));

    assertEquals(WarehouseBusinessException.Kind.NOT_FOUND, exception.kind());
  }

  @Test
  public void testWhenResolveBlankLocationShouldFail() {
    WarehouseBusinessException exception =
        assertThrows(WarehouseBusinessException.class, () -> locationGateway.resolveByIdentifier(""));

    assertEquals(WarehouseBusinessException.Kind.VALIDATION, exception.kind());
  }
}
