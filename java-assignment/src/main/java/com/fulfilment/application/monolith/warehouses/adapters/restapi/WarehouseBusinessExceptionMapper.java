package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.WarehouseBusinessException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;

@Provider
public class WarehouseBusinessExceptionMapper implements ExceptionMapper<WarehouseBusinessException> {

  @Override
  public Response toResponse(WarehouseBusinessException exception) {
    int status =
        exception.kind() == WarehouseBusinessException.Kind.NOT_FOUND
            ? Response.Status.NOT_FOUND.getStatusCode()
            : Response.Status.BAD_REQUEST.getStatusCode();
    return Response.status(status)
        .entity(Map.of("error", exception.getMessage(), "code", status))
        .build();
  }
}
