package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseEndpointTest {

  @Test
  @Order(1)
  public void listsSeedWarehouses() {
    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body(containsString("MWH.001"), containsString("MWH.012"), containsString("MWH.023"));
  }

  @Test
  @Order(2)
  public void getsWarehouseById() {
    given()
        .when()
        .get("/warehouse/2")
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("MWH.012"))
        .body("location", equalTo("AMSTERDAM-001"));
  }

  @Test
  @Order(3)
  public void returnsNotFoundForUnknownWarehouse() {
    given().when().get("/warehouse/999").then().statusCode(404);
  }

  @Test
  @Order(4)
  public void createsWarehouseAtLocationWithRemainingCapacity() {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {"businessUnitCode":"MWH.200","location":"EINDHOVEN-001","capacity":30,"stock":4}
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("MWH.200"))
        .body("location", equalTo("EINDHOVEN-001"));
  }

  @Test
  @Order(5)
  public void rejectsDuplicateBusinessUnitCode() {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {"businessUnitCode":"MWH.001","location":"VETSBY-001","capacity":20,"stock":1}
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(400);
  }

  @Test
  @Order(6)
  public void archivesCreatedWarehouseAndRemovesItFromTheActiveList() {
    String id =
        given()
            .contentType(ContentType.JSON)
            .body(
                """
                {"businessUnitCode":"MWH.201","location":"VETSBY-001","capacity":20,"stock":2}
                """)
            .when()
            .post("/warehouse")
            .then()
            .statusCode(200)
            .extract()
            .path("id");

    given().when().delete("/warehouse/" + id).then().statusCode(204);

    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body(not(containsString("MWH.201")));
  }

  @Test
  @Order(7)
  public void replacesActiveWarehouseKeepingBusinessUnitAndStock() {
    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {"businessUnitCode":"MWH.202","location":"AMSTERDAM-002","capacity":20,"stock":3}
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body(
            """
            {"location":"AMSTERDAM-002","capacity":40,"stock":3}
            """)
        .when()
        .post("/warehouse/MWH.202/replacement")
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("MWH.202"))
        .body("location", equalTo("AMSTERDAM-002"))
        .body("capacity", equalTo(40))
        .body("stock", equalTo(3));
  }
}
