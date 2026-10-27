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

/**
 * HOMEWORK — extend the EXAMPLE in {@link GuiDatabaseOrderTest}.
 * <p>
 * Reference cart (same as {@link sk.testlab.workflows.OrderWorkflow}):
 * KB-001 × 2, MS-001 × 1, coupon STUDENT10, courier → total 8563 cents.
 * Expected after create: KB stock 100→98, MS stock 100→99.
 * After cancel: status cancelled, cancelled_at set, stock back to 100/100.
 * <p>
 * Rules: look up by your own UUID; JDBC stays read-only; cancel only through GUI.
 */
class GuiDatabaseItemsTaskTest {
    private static final String KEYBOARD = "KB-001";
    private static final String MOUSE = "MS-001";

    private WebDriver driver;
    private WebDriverWait wait;
    private TestConfig config;
    private ApiFixtureHelper.Workspace workspace;
    private Artifacts artifacts;
    private OrderDbReader db;
    private String orderId;
    private boolean orderSubmitted;
    private boolean cancelledInTest;

    @Test
    @DisplayName("HOMEWORK: order_items + warehouse + GUI cancel")
    void itemsStockAndCancellationMatchDatabase() throws IOException, SQLException {
        config = TestConfig.load();
        DbConfig dbConfig = DbConfig.load();
        artifacts = new Artifacts("gui-database-homework", config.password, dbConfig.password);

        workspace = ApiFixtureHelper.acquire(config);
        artifacts.put("workspace", workspace.code());

        db = new OrderDbReader(dbConfig);
        long workspaceId = db.workspaceId(workspace.code());

        driver = BrowserFactory.create(config);
        wait = new WebDriverWait(driver, config.timeout);
        OrderWorkflow workflow = new OrderWorkflow(driver, wait, config);
        OrderDetailPage detail = new OrderDetailPage(driver, wait);
        OrderCancelPage cancellation = new OrderCancelPage(driver, wait);

        try {
            // --- given helpers (already available) ---
            // db.findOrder(orderId)
            // db.findItems(orderId)
            // db.stock(workspaceId, sku)
            // detail.getOrderId() / getStatus() / getTotalCents()
            // cancellation.cancelAndWaitUntilCancelled()

            // TODO 1: read starting warehouse stock for KB-001 and MS-001
            //         (fresh fixture should be 100 / 100)

            // TODO 2: create the reference order through the GUI (OrderWorkflow)
            //         save UUID to orderId, set orderSubmitted = true
            //         write artifacts order-id.txt

            // TODO 3: assert GUI shows created + 8563 (same idea as the EXAMPLE)

            // TODO 4: JDBC order_items for that UUID
            //         - exactly 2 rows
            //         - KB-001: unit 3490, qty 2, line 6980
            //         - MS-001: unit 1990, qty 1, line 1990
            //         - sum of line_total_cents == 8970 (subtotal)

            // TODO 5: JDBC warehouse after create
            //         - KB-001 == before - 2
            //         - MS-001 == before - 1

            // TODO 6: cancel the same order through the GUI (OrderCancelPage)
            //         set cancelledInTest = true after success
            //         assert GUI status cancelled

            // TODO 7: JDBC after cancel
            //         - orders.status == cancelled, cancelled_at not null
            //         - total_cents still 8563
            //         - stock back to the values from TODO 1
            //         - order_items unchanged (same two lines)

            throw new UnsupportedOperationException(
                    "Homework: implement TODOs 1–7 (see GuiDatabaseOrderTest for the EXAMPLE pattern).");
        } catch (RuntimeException | AssertionError error) {
            if (artifacts != null) {
                artifacts.capture(driver, "failure", error);
            }
            throw error;
        }
    }

    @AfterEach
    void tearDown() {
        // Same idea as the EXAMPLE: if you created an order but did not cancel it
        // in the test body, cancel only that UUID before releasing the fixture.
        try {
            if (driver != null && wait != null && config != null
                    && orderSubmitted && !cancelledInTest && config.cleanupOrder
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
