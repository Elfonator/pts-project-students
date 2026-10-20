package sk.testlab.gui;

import java.io.IOException;

import org.junit.jupiter.api.*;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.gui.pages.OrderCancelPage;
import sk.testlab.gui.pages.OrderDetailPage;
import sk.testlab.support.Artifacts;
import sk.testlab.support.BrowserFactory;
import sk.testlab.support.TestConfig;
import sk.testlab.workflows.OrderWorkflow;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StableCancelOrderTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private TestConfig config;
    private Artifacts artifacts;

    @BeforeEach
    void setUp(RepetitionInfo repetition) throws IOException {
        config = TestConfig.loadForRun(repetition.getCurrentRepetition());
        artifacts = new Artifacts(repetition.getCurrentRepetition(), config.password);
        artifacts.put("email", config.email);
        artifacts.put("workspace", config.workspace);

        driver = BrowserFactory.create(config);
        wait = new WebDriverWait(driver, config.timeout);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @RepeatedTest(3)
    @DisplayName("Create, cancel and verify the same order")
    void cancelNewlyCreatedOrder() throws IOException {
        OrderWorkflow workflow = new OrderWorkflow(driver, wait, config);
        OrderDetailPage detail = new OrderDetailPage(driver, wait);
        OrderCancelPage cancellation = new OrderCancelPage(driver, wait);

        try {
            // ARRANGE: create a known order in this browser session.
            String expectedOrderId = workflow.createReferenceOrder(config.email, config.password);

            assertFalse(
                    expectedOrderId == null || expectedOrderId.isBlank(),
                    "New order UUID must be displayed");
            artifacts.write("order-id.txt", expectedOrderId);
            artifacts.put("order.id", expectedOrderId);
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
        } catch (RuntimeException | AssertionError error) {
            // Capture before tearDown / driver.quit().
            artifacts.capture(driver, "failure", error);
            throw error;
        }
    }
}
