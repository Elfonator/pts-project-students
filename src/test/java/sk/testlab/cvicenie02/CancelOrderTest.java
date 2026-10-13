package sk.testlab.cvicenie02;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.cvicenie02.support.BrowserFactory;
import sk.testlab.cvicenie02.support.TestConfig;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


class CancelOrderTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private TestConfig config;

    @BeforeEach
    void setUp() {
        // Prepare configuration, browser and wait
        config = TestConfig.load();
        driver = BrowserFactory.create(config);
        wait = new WebDriverWait(driver, config.timeout);
    }

    @AfterEach
    void tearDown() {
        // Close browser after the test
        if (driver != null) {
            driver.quit();
        }
    }

    /* Helpers */
    // Locate element using data-testid
    private By testId(String id) {
        return By.cssSelector("[data-testid='" + id + "']");
    }

    // Wait until element is visible
    private WebElement visible(String id) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(testId(id)));
    }

    // Wait until element is clickable and click
    private void click(String id) {
        wait.until(ExpectedConditions.elementToBeClickable(testId(id))).click();
    }

    // Enter text into an input
    private void type(String id, String value) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(testId(id)));

        input.clear();
        input.sendKeys(value);
    }

    @Test
    @DisplayName("Cancel an existing created order")
    void cancelExistingOrder() {

        // ARRANGE: open application and log in
        driver.get(config.baseUrl);

        click("nav-login");
        type("login-email", config.email);
        type("login-password", config.password);
        click("login-submit");
        visible("current-user");

        // ARRANGE: open order history
        click("nav-orders");

        // Verify that the user has at least one order
        visible("orders-ready");

        /* Different selector examples */
        // CSS selector to find a row containing an order with CSS class "created"
        List<WebElement> createdOrders = driver.findElements(
                        By.cssSelector("tr[data-testid^='order-row-'] span.badge.created")
        );

        // Precondition: there must be an order that can be canceled
        assertFalse(
                createdOrders.isEmpty(),
                "No order with status created is available for cancellation."
        );

        // We found the status badge, but need its table row
        // XPath selector for moving to the parent row
        WebElement createdRow = createdOrders.get(0)
                        .findElement(By.xpath("./ancestor::tr"));

        // Open detail of that order using CSS selector
        WebElement detailLink = createdRow.findElement(By.cssSelector("a[href^='#/orders/']"));
        detailLink.click();

        // Verify that we really opened an order detail
        String orderId = visible("order-id").getText().trim();

        // Verify that selected order is still in created state
        assertEquals(
                "created",
                visible("order-status")
                        .getDomAttribute("data-status"),
                "Selected order must be in created state."
        );

        // ACT
        // start cancellation
        click("cancel-order");

        // confirm cancellation
        click("confirm-cancel");

        // Wait until status changes
        wait.until(
                ExpectedConditions.attributeToBe(
                        testId("order-status"),
                        "data-status",
                        "cancelled"
                )
        );

        // ASSERT
        // verify the final state
        assertEquals(
                "cancelled",
                visible("order-status").getDomAttribute("data-status"),
                "Order was not cancelled."
        );

        // verify we are still looking at the same order
        assertEquals(
                orderId,
                visible("order-id").getText().trim(),
                "Order identity changed during cancellation."
        );

        /*
        // VERSION 2
        // DETERMINISTIC TEST WITH SPECIFIC UUID
        String expectedOrderId = "TL-C48ABDE39D72497E979C6F4EBCD0CCF6";

        assertFalse(
                expectedOrderId == null || expectedOrderId.isBlank(),
                "Missing JVM parameter -Dorder.id=UUID"
        );

        // Open exactly this order
        driver.get(config.baseUrl + "#/orders/" + expectedOrderId);

        // Wait until order detail is displayed
        String actualOrderId =visible("order-id").getText().trim();

        // Verify that the opened detail belongs
        // to the requested UUID.
        assertEquals(
                expectedOrderId,
                actualOrderId,
                "Opened order does not match requested UUID."
        );

        // Verify precondition
        assertEquals(
                "created",
                visible("order-status").getDomAttribute("data-status"),
                "Order must be in created state."
        );

        // Cancel this exact order
        click("cancel-order");
        click("confirm-cancel");

        // Wait for state transition
        wait.until(
                ExpectedConditions.attributeToBe(testId("order-status"),"data-status","cancelled")
        );

        // Verify final state
        assertEquals(
                "cancelled",
                visible("order-status").getDomAttribute("data-status"),
                "Order was not cancelled."
        );

        // Verify we are still on the same order
        assertEquals(
                expectedOrderId,
                visible("order-id").getText().trim(),
                "Order identity changed during cancellation."
        );
        */
    }
}

