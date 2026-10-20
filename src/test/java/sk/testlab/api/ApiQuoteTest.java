package sk.testlab.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import sk.testlab.support.Artifacts;
import sk.testlab.support.TestConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

/** Cvicenie 04 — spoločný happy path; TODO testy študenti samostatne. */
class ApiQuoteTest {
    private TestConfig config;
    private ApiFixtureHelper.Workspace workspace;
    private String token;
    private Artifacts artifacts;

    @BeforeEach
    void setUp(TestInfo info) throws Exception {
        String method = info.getTestMethod().orElseThrow().getName();
        config = TestConfig.load();
        workspace = ApiFixtureHelper.acquire(config);
        artifacts = new Artifacts(method, config.testApiKey, workspace.password());
        artifacts.put("fixtures.mode", config.fixturesMode);
        artifacts.put("workspace.code", workspace.code());
        token = ApiFixtureHelper.login(config, workspace);
    }

    @AfterEach
    void cleanup() {
        if (config == null || workspace == null) return;
        try {
            ApiFixtureHelper.release(config, workspace);
            if (artifacts != null) artifacts.note("cleanup", "workspace-released");
        } catch (Exception e) {
            System.err.println("Fixture cleanup failed: " + e.getClass().getSimpleName());
        }
    }

    @Test
    void quoteHasCorrectTotal() throws Exception {
        var request = Map.of(
                "items", List.of(
                        Map.of("sku", "KB-001", "quantity", 2),
                        Map.of("sku", "MS-001", "quantity", 1)),
                "coupon_code", "STUDENT10",
                "delivery_method", "courier");

        given()
                .filter(artifacts.httpLog("/cart/quote"))
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .queryParam("route", "/cart/quote")
                .body(request)
                .when()
                .post(config.baseUrl + "api/index.php")
                .then()
                .statusCode(200)
                .body("data.subtotal_cents", equalTo(8970))
                .body("data.discount_cents", equalTo(897))
                .body("data.shipping_cents", equalTo(490))
                .body("data.total_cents", equalTo(8563));
        artifacts.put("result", "PASSED");
        artifacts.put("expected.total_cents", 8563);
    }

    @Test
    void invalidQuantityTypeIsRejected() throws IOException {
        var request = Map.of(
                "items", List.of(
                        Map.of("sku", "KB-001", "quantity", "2")));

        given()
                .filter(artifacts.httpLog("/cart/quote"))
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .queryParam("route", "/cart/quote")
                // JSON string "2", not integer 2
                .body(request)
                .when()
                .post(config.baseUrl + "api/index.php")
                .then()
                .statusCode(422)
                .body("error.code", equalTo("VALIDATION_ERROR"));
        artifacts.put("result", "PASSED");
    }

    @Test
    void outOfStockIsRejected() throws IOException {
        var request = Map.of(
                "items", List.of(
                        Map.of("sku", "HUB-001", "quantity", 1)),
                "coupon_code", "",
                "delivery_method", "pickup");
        given()
                .filter(artifacts.httpLog("/cart/quote"))
                .auth().oauth2(token)
                .contentType(ContentType.JSON)
                .queryParam("route", "/cart/quote")
                // HUB-001 stock = 0 in a NEW fixture. Quantity must stay in 1..20.
                .body(request)
                .when()
                .post(config.baseUrl + "api/index.php")
                .then()
                .statusCode(409)
                .body("error.code", equalTo("OUT_OF_STOCK"))
                .body("error.details.available", equalTo(0));
        artifacts.put("result", "PASSED");
    }
}
