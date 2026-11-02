package sk.testlab.db;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

// HOMEWORK solution
class GuiDatabaseItemsTest {
    private static final String KEYBOARD = "KB-001";
    private static final String MOUSE = "MS-001";
    private static final int EXPECTED_TOTAL = 8563;
    private static final int EXPECTED_SUBTOTAL = 8970;

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
            // TODO 1: starting warehouse stock
            int keyboardBefore = db.stock(workspaceId, KEYBOARD);
            int mouseBefore = db.stock(workspaceId, MOUSE);
            artifacts.put("stock.before.kb", keyboardBefore);
            artifacts.put("stock.before.ms", mouseBefore);

            assertAll("Fresh fixture stock",
                    () -> assertEquals(100, keyboardBefore, "KB-001 must start at 100"),
                    () -> assertEquals(100, mouseBefore, "MS-001 must start at 100")
            );

            // TODO 2: create reference order through GUI
            orderId = workflow.createReferenceOrder(workspace.email(), workspace.password());
            orderSubmitted = true;
            artifacts.write("order-id.txt", orderId);
            artifacts.put("order.id", orderId);

            // TODO 3: GUI after create
            assertAll("GUI after create",
                    () -> assertFalse(orderId == null || orderId.isBlank(), "Order UUID must be shown"),
                    () -> assertEquals("8563", detail.getTotalCents()),
                    () -> assertEquals("created", detail.getStatus())
            );

            // TODO 4: JDBC order_items
            List<OrderItemRecord> itemsAfterCreate = db.findItems(orderId);
            Map<String, OrderItemRecord> bySku = itemsBySku(itemsAfterCreate);
            OrderItemRecord keyboardItem = bySku.get(KEYBOARD);
            OrderItemRecord mouseItem = bySku.get(MOUSE);

            assertAll("order_items after create",
                    () -> assertEquals(2, itemsAfterCreate.size(), "exactly two line items"),
                    () -> assertEquals(orderId, keyboardItem.orderId()),
                    () -> assertEquals(3490, keyboardItem.unitPriceCents()),
                    () -> assertEquals(2, keyboardItem.quantity()),
                    () -> assertEquals(6980, keyboardItem.lineTotalCents()),
                    () -> assertEquals(orderId, mouseItem.orderId()),
                    () -> assertEquals(1990, mouseItem.unitPriceCents()),
                    () -> assertEquals(1, mouseItem.quantity()),
                    () -> assertEquals(1990, mouseItem.lineTotalCents()),
                    () -> assertEquals(
                            EXPECTED_SUBTOTAL,
                            itemsAfterCreate.stream().mapToInt(OrderItemRecord::lineTotalCents).sum(),
                            "line totals sum to subtotal")
            );

            // TODO 5: warehouse after create
            int keyboardAfterCreate = db.stock(workspaceId, KEYBOARD);
            int mouseAfterCreate = db.stock(workspaceId, MOUSE);
            artifacts.put("stock.after.create.kb", keyboardAfterCreate);
            artifacts.put("stock.after.create.ms", mouseAfterCreate);

            assertAll("Warehouse after create",
                    () -> assertEquals(keyboardBefore - 2, keyboardAfterCreate, "KB-001 -2"),
                    () -> assertEquals(mouseBefore - 1, mouseAfterCreate, "MS-001 -1")
            );

            // TODO 6: cancel through GUI
            cancellation.cancelAndWaitUntilCancelled();
            cancelledInTest = true;
            assertEquals("cancelled", detail.getStatus(), "GUI status after cancel");

            // TODO 7: JDBC after cancel
            OrderRecord cancelled = db.findOrder(orderId);
            List<OrderItemRecord> itemsAfterCancel = db.findItems(orderId);
            int keyboardAfterCancel = db.stock(workspaceId, KEYBOARD);
            int mouseAfterCancel = db.stock(workspaceId, MOUSE);
            artifacts.put("db.status.cancelled", cancelled.status());
            artifacts.put("stock.after.cancel.kb", keyboardAfterCancel);
            artifacts.put("stock.after.cancel.ms", mouseAfterCancel);

            assertAll("DB after cancel",
                    () -> assertEquals("cancelled", cancelled.status()),
                    () -> assertNotNull(cancelled.cancelledAt(), "cancelled_at must be set"),
                    () -> assertEquals(EXPECTED_TOTAL, cancelled.totalCents(), "price unchanged"),
                    () -> assertEquals(keyboardBefore, keyboardAfterCancel, "KB-001 stock restored"),
                    () -> assertEquals(mouseBefore, mouseAfterCancel, "MS-001 stock restored"),
                    () -> assertEquals(itemsAfterCreate, itemsAfterCancel, "order_items unchanged")
            );

            artifacts.put("result", "PASSED");
            System.out.println(
                    "PASS (homework): " + orderId
                            + " | items OK | stock 100/100 -> 98/99 -> 100/100"
                            + " | created -> cancelled");
        } catch (RuntimeException | AssertionError | SQLException error) {
            if (artifacts != null) {
                artifacts.capture(driver, "failure", error);
            }
            throw error;
        }
    }

    private static Map<String, OrderItemRecord> itemsBySku(List<OrderItemRecord> items) {
        return items.stream().collect(Collectors.toMap(
                OrderItemRecord::sku,
                Function.identity(),
                (a, b) -> {
                    throw new IllegalStateException("Duplicate SKU in order_items: " + a.sku());
                }
        ));
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
