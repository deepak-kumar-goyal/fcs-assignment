package com.fulfilment.application.monolith.fulfilment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfilmentEndpointTest {

  @Test
  public void associatesAndListsFulfilmentUnits() {
    Integer assignmentId =
        given()
            .contentType(ContentType.JSON)
            .body("{\"productId\":2,\"warehouseBusinessUnitCode\":\"MWH.023\"}")
            .when()
            .post("/store/2/fulfilment")
            .then()
            .statusCode(201)
            .body("storeId", equalTo(2))
            .body("productId", equalTo(2))
            .body("warehouseBusinessUnitCode", equalTo("MWH.023"))
            .extract()
            .path("id");

    given()
        .when()
        .get("/store/2/fulfilment")
        .then()
        .statusCode(200)
        .body("[0].warehouseBusinessUnitCode", equalTo("MWH.023"));

    given().when().delete("/store/2/fulfilment/" + assignmentId).then().statusCode(204);
  }

  @Test
  public void rejectsUnknownStore() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":2,\"warehouseBusinessUnitCode\":\"MWH.023\"}")
        .when()
        .post("/store/999/fulfilment")
        .then()
        .statusCode(404);
  }

  @Test
  public void rejectsUnknownWarehouse() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":3,\"warehouseBusinessUnitCode\":\"MISSING\"}")
        .when()
        .post("/store/3/fulfilment")
        .then()
        .statusCode(404);
  }
}
