package com.fulfilment.application.monolith.warehouses.domain;

public class WarehouseBusinessException extends RuntimeException {

  public enum Kind {
    NOT_FOUND,
    VALIDATION
  }

  private final Kind kind;

  public WarehouseBusinessException(Kind kind, String message) {
    super(message);
    this.kind = kind;
  }

  public Kind kind() {
    return kind;
  }

  public static WarehouseBusinessException notFound(String message) {
    return new WarehouseBusinessException(Kind.NOT_FOUND, message);
  }

  public static WarehouseBusinessException validation(String message) {
    return new WarehouseBusinessException(Kind.VALIDATION, message);
  }
}
