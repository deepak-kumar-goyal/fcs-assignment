package com.fulfilment.application.monolith.fulfilment.adapters.restapi;

import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssignment;

public class FulfilmentAssociationResponse {

  public Long id;
  public Long storeId;
  public Long productId;
  public String warehouseBusinessUnitCode;

  public static FulfilmentAssociationResponse from(FulfilmentAssignment assignment) {
    FulfilmentAssociationResponse response = new FulfilmentAssociationResponse();
    response.id = assignment.id;
    response.storeId = assignment.store.id;
    response.productId = assignment.product.id;
    response.warehouseBusinessUnitCode = assignment.warehouseBusinessUnitCode;
    return response;
  }
}
