package sk.testlab.db;

import java.io.IOException;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import sk.testlab.api.ApiFixtureHelper;
import sk.testlab.gui.pages.OrderCancelPage;
import sk.testlab.gui.pages.OrderDetailPage;
import sk.testlab.support.Artifacts;
import sk.testlab.support.BrowserFactory;
import sk.testlab.support.TestConfig;
import sk.testlab.workflows.OrderWorkflow;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * EXAMPLE (run this first): create an order in the GUI, take the UUID from the
 * detail page, then verify the same row in {@code orders} via read-only JDBC.
 * <p>
 * Homework continues in {@link GuiDatabaseItemsTaskTest}
 * (order_items, warehouse stock, cancellation).
 */
class GuiDatabaseOrderTest {
    private static final int EXPECTED_SUBTOTAL = 8970;
    private static final int EXPECTED_DISCOUNT = 897;
    private static final int EXPECTED_SHIPPING = 490;
    private static final int EXPECTED_TOTAL = 8563;

    private WebDriver driver;
    private WebDriverWait wait;
    private TestConfig config;
    private ApiFixtureHelper.Workspace workspace;
    private Artifacts artifacts;
    private OrderDbReader db;
    private String orderId;
    private boolean orderSubmitted;

    @Test
    @DisplayName("EXAMPLE: GUI create → UUID → JDBC orders check")
    void guiOrderMatchesOrdersTableByUuid() throws IOException, SQLException {
        config = TestConfig.load();
        DbConfig dbConfig = DbConfig.load();
        artifacts = new Artifacts("gui-database-order", config.password, dbConfig.password);

        workspace = ApiFixtureHelper.acquire(config);
        artifacts.put("workspace", workspace.code());
        artifacts.put("fixtures.mode", config.fixturesMode);

        db = new OrderDbReader(dbConfig);
        long expectedWorkspaceId = db.workspaceId(workspace.code());
        artifacts.put("workspace.id", expectedWorkspaceId);

        driver = BrowserFactory.create(config);
        wait = new WebDriverWait(driver, config.timeout);
        OrderWorkflow workflow = new OrderWorkflow(driver, wait, config);
        OrderDetailPage detail = new OrderDetailPage(driver, wait);

        try {
            // ACT: order exists only after GUI submit. UUID comes from our screen —
            // never SELECT … ORDER BY created_at DESC LIMIT 1
            orderId = workflow.createReferenceOrder(workspace.email(), workspace.password());
            orderSubmitted = true;
            artifacts.write("order-id.txt", orderId);
            artifacts.put("order.id", orderId);

            assertAll("GUI after create",
                    () -> assertFalse(orderId == null || orderId.isBlank(), "Order UUID must be shown"),
                    () -> assertEquals("8563", detail.getTotalCents()),
                    () -> assertEquals("85,63 €", detail.getVisibleTotal()),
                    () -> assertEquals("created", detail.getStatus())
            );

            // ASSERT: independent DB view of the same UUID.
            OrderRecord created = db.findOrder(orderId);
            artifacts.put("db.status", created.status());
            artifacts.put("db.workspace.id", created.workspaceId());

            assertAll("orders row for our UUID",
                    () -> assertEquals(orderId, created.id()),
                    () -> assertEquals(
                            expectedWorkspaceId, created.workspaceId(),
                            "Order must belong to the fixture workspace"),
                    () -> assertEquals("created", created.status()),
                    () -> assertEquals(EXPECTED_SUBTOTAL, created.subtotalCents()),
                    () -> assertEquals(EXPECTED_DISCOUNT, created.discountCents()),
                    () -> assertEquals(EXPECTED_SHIPPING, created.shippingCents()),
                    () -> assertEquals(EXPECTED_TOTAL, created.totalCents()),
                    () -> assertEquals("STUDENT10", created.couponCode()),
                    () -> assertEquals("courier", created.deliveryMethod()),
                    () -> assertNotNull(created.createdAt(), "created_at must be set"),
                    () -> assertNull(created.cancelledAt(), "new order has no cancelled_at")
            );

            artifacts.put("result", "PASSED");
            System.out.println("PASS (example): " + orderId + " | orders total_cents=8563 | workspace_id ok");
        } catch (RuntimeException | AssertionError | SQLException error) {
            if (artifacts != null) {
                artifacts.capture(driver, "failure", error);
            }
            throw error;
        }
    }

    @AfterEach
    void tearDown() {
        // If the example stops after create, cancel only OUR uuid so the fixture
        // does not keep a live order (stock would stay reduced).
        try {
            if (driver != null && wait != null && config != null
                    && orderSubmitted && config.cleanupOrder
                    && orderId != null && !orderId.isBlank()) {
                driver.get(config.baseUrl + "#/orders/" + orderId);
                new OrderCancelPage(driver, wait).cancelAndWaitUntilCancelled();
                if (artifacts != null) {
                    artifacts.note("cleanup", "CANCELLED_OWN_ORDER");
                }
            }
        } catch (Exception e) {
            System.err.println("Order cleanup failed: " + e.getClass().getSimpleName());
            if (artifacts != null) {
                artifacts.note("cleanup", "FAILED_" + e.getClass().getSimpleName());
            }
        }

        if (db != null) {
            try {
                db.close();
            } catch (SQLException e) {
                System.err.println("DB close failed: " + e.getMessage());
            }
        }
        if (driver != null) {
            driver.quit();
        }
        try {
            if (config != null && workspace != null) {
                ApiFixtureHelper.release(config, workspace);
            }
        } catch (Exception e) {
            System.err.println("Fixture cleanup failed: " + e.getClass().getSimpleName());
        }
    }
}
