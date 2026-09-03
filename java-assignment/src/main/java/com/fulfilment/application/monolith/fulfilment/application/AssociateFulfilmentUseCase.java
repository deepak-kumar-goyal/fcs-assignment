package com.fulfilment.application.monolith.fulfilment.application;

import com.fulfilment.application.monolith.fulfilment.adapters.database.FulfilmentAssignmentRepository;
import com.fulfilment.application.monolith.fulfilment.domain.model.FulfilmentAssignment;
import com.fulfilment.application.monolith.fulfilment.domain.validator.FulfilmentValidator;
import com.fulfilment.application.monolith.fulfilment.domain.validator.FulfilmentValidator.ValidatedAssociation;
import com.fulfilment.application.monolith.stores.Store;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AssociateFulfilmentUseCase {

  private final FulfilmentAssignmentRepository assignments;
  private final FulfilmentValidator validator;

  @Inject
  public AssociateFulfilmentUseCase(
      FulfilmentAssignmentRepository assignments, FulfilmentValidator validator) {
    this.assignments = assignments;
    this.validator = validator;
  }

  public FulfilmentAssignment associate(
      Store store, Long productId, String warehouseBusinessUnitCode) {
    ValidatedAssociation validated =
        validator.validate(store, productId, warehouseBusinessUnitCode);

    FulfilmentAssignment assignment = new FulfilmentAssignment();
    assignment.store = store;
    assignment.product = validated.product();
    assignment.warehouseBusinessUnitCode = validated.warehouse().businessUnitCode;
    assignments.persist(assignment);
    return assignment;
  }
}
