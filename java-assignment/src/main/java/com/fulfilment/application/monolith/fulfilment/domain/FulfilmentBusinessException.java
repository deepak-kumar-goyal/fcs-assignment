package com.fulfilment.application.monolith.fulfilment.domain;

public class FulfilmentBusinessException extends RuntimeException {

  public enum Kind {
    VALIDATION,
    NOT_FOUND
  }

  private final Kind kind;

  private FulfilmentBusinessException(Kind kind, String message) {
    super(message);
    this.kind = kind;
  }

  public static FulfilmentBusinessException validation(String message) {
    return new FulfilmentBusinessException(Kind.VALIDATION, message);
  }

  public static FulfilmentBusinessException notFound(String message) {
    return new FulfilmentBusinessException(Kind.NOT_FOUND, message);
  }

  public Kind kind() {
    return kind;
  }
}
