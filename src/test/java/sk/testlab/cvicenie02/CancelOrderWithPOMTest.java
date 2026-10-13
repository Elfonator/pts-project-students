package sk.testlab.cvicenie02;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.cvicenie02.pages.OrderCancelPage;
import sk.testlab.cvicenie02.pages.OrderDetailPage;
import sk.testlab.cvicenie02.support.BrowserFactory;
import sk.testlab.cvicenie02.support.TestConfig;
import sk.testlab.cvicenie02.workflows.OrderWorkflow;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CancelOrderWithPOMTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private TestConfig config;

    @BeforeEach
    void setUp() {
        config = TestConfig.load();
        driver = BrowserFactory.create(config);
        wait = new WebDriverWait(driver, config.timeout);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Create, cancel and verify the same order")
    void cancelNewlyCreatedOrder() {
        OrderWorkflow workflow = new OrderWorkflow(driver, wait, config);
        OrderDetailPage detail = new OrderDetailPage(driver, wait);
        OrderCancelPage cancellation = new OrderCancelPage(driver, wait);

        // ARRANGE: create a known order in this browser session.
        String expectedOrderId = workflow.createReferenceOrder(config.email, config.password);

        assertFalse(
                expectedOrderId == null || expectedOrderId.isBlank(),
                "New order UUID must be displayed");
        assertEquals(
                "created", detail.getStatus(),
                "New order must be cancellable");

        // ACT: cancel this order while remaining on its detail screen
        cancellation.cancelAndWaitUntilCancelled();

        // ASSERT: confirm state change and identity
        assertAll("Cancelled order",
                () -> assertEquals("cancelled", detail.getStatus(),
                        "Order should be cancelled"),
                () -> assertEquals(expectedOrderId, detail.getOrderId(),
                        "Cancellation must not navigate to a different order")
        );
    }
}