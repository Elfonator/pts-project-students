package sk.testlab.gui.examples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.support.BrowserFactory;
import sk.testlab.support.TestConfig;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateOrderTest {

    // Expected values come from the test specification.
    private static final String EXPECTED_TOTAL_CENTS = "8563";
    private static final String EXPECTED_VISIBLE_TOTAL = "85,63 €";
    private static final String EXPECTED_STATUS = "created";

    private WebDriver driver;
    private WebDriverWait wait;
    private TestConfig config;

    @BeforeEach
    void setUp() {
        // Prepare configuration, browser and explicit wait.
        config = TestConfig.load();
        driver = BrowserFactory.create(config);
        wait = new WebDriverWait(driver, config.timeout);
    }

    @AfterEach
    void tearDown() {
        // Always close the browser after the test.
        if (driver != null) {
            driver.quit();
        }
    }

    // Locate an element by its stable data-testid
    private By testId(String id) {
        return By.cssSelector("[data-testid='" + id + "']");
    }

    // Wait until an element is visible
    private WebElement visible(String id) {
        return wait.until(
                ExpectedConditions.visibilityOfElementLocated(testId(id))
        );
    }

    // Wait until an element is clickable and click on it
    private void click(String id) {
        wait.until(
                ExpectedConditions.elementToBeClickable(testId(id))
        ).click();
    }

    // Clear an input and enter text
    private void type(String id, String value) {
        WebElement input = wait.until(
                ExpectedConditions.elementToBeClickable(testId(id))
        );
        input.clear();
        input.sendKeys(value);
    }

    // Save generated order UUID as test evidence
    private void saveOrderId(String orderId) throws IOException {
        Path directory = Path.of("target", "artifacts");
        Files.createDirectories(directory);

        Files.writeString(
                directory.resolve("order-id.txt"),
                orderId
        );
    }

    // Save screenshot when test fails
    private void saveFailureScreenshot() {
        if (!(driver instanceof TakesScreenshot camera)) {
            return;
        }

        try {
            Path directory = Path.of("target", "artifacts");
            Files.createDirectories(directory);

            Files.write(
                    directory.resolve("failure.png"),
                    camera.getScreenshotAs(OutputType.BYTES)
            );
        } catch (IOException error) {
            System.err.println(
                    "Screenshot could not be saved: " + error.getMessage()
            );
        }
    }

    // Normalize spaces used when formatting currency
    private String normalizeSpaces(String text) {
        return text.replaceAll("[\\s\\p{Zs}]+", " ").trim();
    }

    @Test
    @DisplayName("Create reference order and verify its result")
    void createReferenceOrder() throws IOException {

        try {
            // ARRANGE: open application and log in
            driver.get(config.baseUrl);

            click("nav-login");
            type("login-email", config.email);
            type("login-password", config.password);
            click("login-submit");

            visible("current-user");
            visible("catalog-ready");

            // ACT: add products to the cart
            click("add-to-cart-KB-001");
            wait.until(ExpectedConditions.textToBe(testId("cart-count"), "1"));

            click("add-to-cart-KB-001");
            wait.until(ExpectedConditions.textToBe(testId("cart-count"), "2"));

            click("add-to-cart-MS-001");
            wait.until(ExpectedConditions.textToBe(testId("cart-count"), "3"));

            // ACT: configure the cart
            click("nav-cart");
            new Select(visible("delivery-method")).selectByValue("courier");

            // ACT: apply coupon and wait for recalculation
            WebElement previousQuote = visible("quote-total");

            type("coupon-input", "STUDENT10");
            click("coupon-apply");

            wait.until(ExpectedConditions.stalenessOf(previousQuote));
            visible("quote-total");

            // ACT: complete checkout
            click("proceed-checkout");
            visible("checkout-form");

            type("customer-name", "Student Testovaci");
            type("customer-email", "student@example.test");
            type("customer-address", "Testovacia 12");
            type("customer-city", "Nitra");
            type("customer-postal-code", "949 01");

            click("accept-terms");

            // ACT: create the order
            click("place-order");

            // Read and store the identity of this exact order
            String orderId = visible("order-id")
                    .getText()
                    .trim();

            saveOrderId(orderId);

            // Read actual values produced by the application
            WebElement total = visible("order-total");
            String actualCents = total.getDomAttribute("data-cents");
            String actualVisibleTotal = normalizeSpaces(total.getText());
            String actualStatus = visible("order-status").getDomAttribute("data-status");

            // ASSERT: compare actual values with the specification
            assertAll(
                    "Reference order",
                    () -> assertFalse(
                            orderId.isBlank(),
                            "Order UUID must be displayed."),

                    () -> assertEquals(
                            EXPECTED_TOTAL_CENTS,
                            actualCents,
                            "Incorrect total in cents."
                    ),

                    () -> assertEquals(
                            EXPECTED_VISIBLE_TOTAL,
                            actualVisibleTotal,
                            "Incorrect visible total."
                    ),

                    () -> assertEquals(
                            EXPECTED_STATUS,
                            actualStatus,
                            "Incorrect order status."
                    )
            );

        } catch (RuntimeException | AssertionError error) {
            // Capture evidence before @AfterEach closes the browser
            saveFailureScreenshot();
            throw error;
        }
    }
}