package com.fulfilment.application.monolith.fulfillment;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class FulfillmentResourceTest {

    @Test
    public void testAssignFulfillmentSuccess() {
        String payload = """
        {
          "storeId": 1,
          "productId": 1,
          "warehouseBuCode": "MWH.001"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when().post("/fulfillment/assign")
                .then()
                .statusCode(201)
                .body("id", notNullValue());
    }

    @Test
    public void testAssignFulfillmentNonExistentWarehouse() {
        String payload = """
        {
          "storeId": 1,
          "productId": 1,
          "warehouseBuCode": "NON_EXISTENT_WAREHOUSE"
        }
        """;

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when().post("/fulfillment/assign")
                .then()
                .statusCode(404);
    }

    @Test
    public void testAssignFulfillmentEmptyRequest() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when().post("/fulfillment/assign")
                .then()
                .statusCode(400);
    }
}