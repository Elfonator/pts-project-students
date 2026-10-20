package sk.testlab.api;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import sk.testlab.support.Artifacts;
import sk.testlab.support.TestConfig;

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
    void invalidQuantityTypeIsRejected() {
        // TODO 1: POST /cart/quote so sku KB-001 a quantity ako STRING "2".
        // TODO 2: over HTTP 422.
        // TODO 3: over error.code = VALIDATION_ERROR.
        // Odstran nasledujucu vynimku po implementacii.
        throw new UnsupportedOperationException("TODO student: invalidQuantityTypeIsRejected");
    }

    @Test
    void outOfStockIsRejected() {
        // BONUS TODO: sku HUB-001, quantity integer 1, pickup, bez kuponu.
        // Over status 409, error.code = OUT_OF_STOCK, error.details.available = 0.
        throw new UnsupportedOperationException("BONUS TODO student: outOfStockIsRejected");
    }
}
