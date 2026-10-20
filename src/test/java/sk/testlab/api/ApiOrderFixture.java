package sk.testlab.api;

import io.restassured.http.ContentType;
import sk.testlab.support.TestConfig;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;

// Creates exactly one order for a GUI test using an already existing fixture account
public final class ApiOrderFixture {
    private ApiOrderFixture() {}

    // Prefer ApiFixtureHelper.login when the account came from an automatic workspace
    public static String getBearer(TestConfig config) {
        return ApiFixtureHelper.login(config, config.email, config.password);
    }

    public static String createReferenceOrder(TestConfig config, String bearer) {
        var body = Map.of(
                "items", List.of(
                        Map.of("sku", "KB-001", "quantity", 2),
                        Map.of("sku", "MS-001", "quantity", 1)),
                "coupon_code", "STUDENT10",
                "delivery_method", "courier",
                "customer", Map.of(
                        "name", "API pripraveny test",
                        "email", "test@example.test",
                        "address", "Testovacia 12",
                        "city", "Nitra",
                        "postal_code", "949 01"),
                "note", "Created by REST Assured, verified via GUI",
                "accept_terms", true,
                "expected_total_cents", 8563);
        return given()
                .auth().oauth2(bearer)
                .contentType(ContentType.JSON)
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .queryParam("route", "/orders")
                .body(body)
                .when()
                .post(config.baseUrl + "api/index.php")
                .then()
                .statusCode(201)
                .body("data.status", equalTo("created"))
                .body("data.total_cents", equalTo(8563))
                .body("data.id", not(blankOrNullString()))
                .extract().path("data.id");
    }

    // Safe on an already-cancelled order. Call only with the UUID made by this test
    public static void cancelOrder(TestConfig config, String bearer, String orderId) {
        given()
                .auth().oauth2(bearer)
                .contentType(ContentType.JSON)
                .queryParam("route", "/orders/" + orderId + "/cancel")
                .body(Map.of())
                .when()
                .post(config.baseUrl + "api/index.php")
                .then()
                .statusCode(200)
                .body("data.id", equalTo(orderId))
                .body("data.status", equalTo("cancelled"));
    }
}
