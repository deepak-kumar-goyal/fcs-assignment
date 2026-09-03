package com.fulfilment.application.monolith.fulfilment.adapters.restapi;

import com.fulfilment.application.monolith.fulfilment.adapters.database.FulfilmentAssignmentRepository;
import com.fulfilment.application.monolith.fulfilment.application.AssociateFulfilmentUseCase;
import com.fulfilment.application.monolith.fulfilment.domain.FulfilmentBusinessException;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssignment;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("store/{storeId}/fulfilment")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfilmentResource {

  @Inject AssociateFulfilmentUseCase associateFulfilmentUseCase;
  @Inject FulfilmentAssignmentRepository assignmentRepository;

  @GET
  public List<FulfilmentAssociationResponse> list(@PathParam("storeId") Long storeId) {
    Store store = requireStore(storeId);
    return assignmentRepository.findByStoreId(store.id).stream()
        .map(FulfilmentAssociationResponse::from)
        .toList();
  }

  @POST
  @Transactional
  public Response create(
      @PathParam("storeId") Long storeId, FulfilmentAssociationRequest request) {
    try {
      Store store = requireStore(storeId);
      if (request == null) {
        throw FulfilmentBusinessException.validation(
            "Fulfilment association payload is required");
      }
      FulfilmentAssignment assignment =
          associateFulfilmentUseCase.associate(
              store, request.productId, request.warehouseBusinessUnitCode);
      return Response.status(Response.Status.CREATED)
          .entity(FulfilmentAssociationResponse.from(assignment))
          .build();
    } catch (FulfilmentBusinessException exception) {
      throw toHttp(exception);
    }
  }

  @DELETE
  @Path("{id}")
  @Transactional
  public Response delete(@PathParam("storeId") Long storeId, @PathParam("id") Long id) {
    requireStore(storeId);
    FulfilmentAssignment assignment = assignmentRepository.findById(id);
    if (assignment == null || !assignment.store.id.equals(storeId)) {
      throw new WebApplicationException(
          "Fulfilment association " + id + " does not exist.", 404);
    }
    assignmentRepository.delete(assignment);
    return Response.status(Response.Status.NO_CONTENT).build();
  }

  private static Store requireStore(Long storeId) {
    Store store = Store.findById(storeId);
    if (store == null) {
      throw new WebApplicationException(
          "Store with id of " + storeId + " does not exist.", 404);
    }
    return store;
  }

  private static WebApplicationException toHttp(FulfilmentBusinessException exception) {
    int status =
        exception.kind() == FulfilmentBusinessException.Kind.NOT_FOUND ? 404 : 400;
    return new WebApplicationException(exception.getMessage(), status);
  }
}
