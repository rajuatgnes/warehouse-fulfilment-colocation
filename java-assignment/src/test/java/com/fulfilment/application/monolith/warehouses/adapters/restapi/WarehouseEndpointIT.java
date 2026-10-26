package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class WarehouseEndpointIT {

  @Test
  public void testListWarehouses() {
    given()
            .when().get("/warehouse")
            .then()
            .statusCode(200)
            .body("size()", greaterThanOrEqualTo(1));
  }

  @Test
  public void testGetWarehouseById() {
    given()
            .when().get("/warehouse/MWH.001")
            .then()
            .statusCode(200)
            .body("businessUnitCode", is("MWH.001"));
  }

  @Test
  public void testGetWarehouseByIdNotFound() {
    given()
            .when().get("/warehouse/NONEXISTENT_BUC")
            .then()
            .statusCode(404);
  }

  @Test
  public void testCreateWarehouseValidationFailure() {
    String payload = """
        {
          "businessUnitCode": "MWH.001",
          "location": "AMSTERDAM-001",
          "capacity": 50,
          "stock": 10
        }
        """;
    given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when().post("/warehouse")
            .then()
            .statusCode(400);
  }

  @Test
  public void testReplaceWarehouseNotFound() {
    String payload = """
        {
          "businessUnitCode": "MWH.UNKNOWN",
          "location": "AMSTERDAM-001",
          "capacity": 50,
          "stock": 10
        }
        """;
    given()
            .contentType(ContentType.JSON)
            .body(payload)
            .when().post("/warehouse/MWH.UNKNOWN/replacement")
            .then()
            .statusCode(404);
  }
}