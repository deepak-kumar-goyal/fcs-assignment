package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreEndpointTest {

  @Test
  public void listsStores() {
    given()
        .when()
        .get("/store")
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"));
  }

  @Test
  public void createsAndUpdatesStoreAfterPersist() {
    Integer id =
        given()
            .contentType(ContentType.JSON)
            .body("{\"name\":\"NEWSTORE\",\"quantityProductsInStock\":7}")
            .when()
            .post("/store")
            .then()
            .statusCode(201)
            .body("name", equalTo("NEWSTORE"))
            .extract()
            .path("id");

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"NEWSTORE-UPD\",\"quantityProductsInStock\":9}")
        .when()
        .put("/store/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("NEWSTORE-UPD"))
        .body("quantityProductsInStock", equalTo(9));

    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"NEWSTORE-PATCH\"}")
        .when()
        .patch("/store/" + id)
        .then()
        .statusCode(200)
        .body("name", equalTo("NEWSTORE-PATCH"));

    given().when().get("/store/" + id).then().statusCode(200).body("name", equalTo("NEWSTORE-PATCH"));

    given().when().delete("/store/" + id).then().statusCode(204);
  }

  @Test
  public void returnsNotFoundForUnknownStore() {
    given().when().get("/store/9999").then().statusCode(404);
  }
}
