package com.fulfilment.application.monolith.stores;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class StoreResourceTest {

    @Test
    public void testListStores() {
        given()
                .when().get("/store")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1));
    }

    @Test
    public void testGetSingleStore() {
        given()
                .when().get("/store/1")
                .then()
                .statusCode(200)
                .body("name", is("TONSTAD"));
    }

    @Test
    public void testGetStoreNotFound() {
        given()
                .when().get("/store/9999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateStore() {
        String payload = """
        {
          "name": "NEW_STORE",
          "quantityProductsInStock": 15
        }
        """;
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when().post("/store")
                .then()
                .statusCode(201)
                .body("name", is("NEW_STORE"));
    }

    @Test
    public void testCreateStoreWithIdThrows422() {
        String payload = """
        {
          "id": 99,
          "name": "INVALID_ID_STORE"
        }
        """;
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when().post("/store")
                .then()
                .statusCode(422);
    }

    @Test
    public void testUpdateStore() {
        String payload = """
        {
          "name": "KALLAX_UPDATED",
          "quantityProductsInStock": 20
        }
        """;
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when().put("/store/2")
                .then()
                .statusCode(200)
                .body("name", is("KALLAX_UPDATED"));
    }

    @Test
    public void testPatchStore() {
        String payload = """
        {
          "name": "BESTA_PATCHED",
          "quantityProductsInStock": 30
        }
        """;
        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when().patch("/store/3")
                .then()
                .statusCode(200)
                .body("name", is("BESTA_PATCHED"));
    }

    @Test
    public void testDeleteStoreNotFound() {
        given()
                .when().delete("/store/8888")
                .then()
                .statusCode(404);
    }
}