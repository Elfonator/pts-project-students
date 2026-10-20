package sk.testlab.api;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import sk.testlab.support.TestConfig;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;

/**
 * Opens a workspace for an API test.
 * fixtures.mode=api → POST /testing/fixtures (Docker test API), then delete after the test.
 * fixtures.mode=manual → reuse test.email/password from config (never delete).
 */
public final class ApiFixtureHelper {
    private ApiFixtureHelper() {}

    public record Workspace(String code, String email, String password, boolean owned) {}

    // Create (api) or reuse configured account (manual)
    public static Workspace acquire(TestConfig config) {
        if (config.automaticFixtures()) {
            return create(config);
        }
        if (config.email == null || config.email.isBlank()
                || config.password == null || config.password.isBlank()) {
            throw new IllegalStateException(
                    "fixtures.mode=manual vyžaduje test.email a test.password v config/test.properties.");
        }
        return new Workspace(config.workspace, config.email, config.password, false);
    }

    public static Workspace create(TestConfig config) {
        config.requireTestApiKey();
        ValidatableResponse response = given()
                .contentType(ContentType.JSON)
                .header("X-Test-Key", config.testApiKey)
                .queryParam("route", "/testing/fixtures")
                .body("{}")
                .when()
                .post(config.baseUrl + "api/index.php")
                .then()
                .statusCode(201)
                .body("data.workspace_code", matchesPattern("run-[a-f0-9]{24}"))
                .body("data.email", not(blankOrNullString()))
                .body("data.password", not(blankOrNullString()));
        return new Workspace(
                response.extract().path("data.workspace_code"),
                response.extract().path("data.email"),
                response.extract().path("data.password"),
                true);
    }

    public static String login(TestConfig config, String email, String password) {
        return given()
                .contentType(ContentType.JSON)
                .queryParam("route", "/auth/token")
                .body(Map.of("email", email, "password", password))
                .when()
                .post(config.baseUrl + "api/index.php")
                .then()
                .statusCode(201)
                .extract().path("data.token");
    }

    public static String login(TestConfig config, Workspace workspace) {
        return login(config, workspace.email(), workspace.password());
    }

    /** Deletes only a workspace this helper created (owned=true). Safe no-op for manual. */
    public static void release(TestConfig config, Workspace workspace) {
        if (workspace == null || !workspace.owned()) return;
        delete(config, workspace.code());
    }

    /** Deletes only the run-* workspace created by this test. Never call with demo. */
    public static void delete(TestConfig config, String workspaceCode) {
        config.requireTestApiKey();
        if (workspaceCode == null || !workspaceCode.matches("run-[a-f0-9]{24}")) {
            throw new IllegalArgumentException("Delete only a run-* workspace created by this test.");
        }
        given()
                .header("X-Test-Key", config.testApiKey)
                .queryParam("route", "/testing/fixtures/" + workspaceCode)
                .when()
                .delete(config.baseUrl + "api/index.php")
                .then()
                .statusCode(200)
                .body("data.workspace_code", equalTo(workspaceCode))
                .body("data.deleted", equalTo(true));
    }
}
