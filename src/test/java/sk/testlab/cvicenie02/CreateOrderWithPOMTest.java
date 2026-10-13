package sk.testlab.cvicenie02;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import sk.testlab.cvicenie02.pages.CartPage;
import sk.testlab.cvicenie02.pages.CatalogPage;
import sk.testlab.cvicenie02.pages.CheckoutPage;
import sk.testlab.cvicenie02.pages.LoginPage;
import sk.testlab.cvicenie02.pages.OrderDetailPage;

import sk.testlab.cvicenie02.support.BrowserFactory;
import sk.testlab.cvicenie02.support.TestConfig;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateOrderWithPOMTest {
    private static final String EXPECTED_TOTAL_CENTS = "8563";
    private static final String EXPECTED_VISIBLE_TOTAL = "85,63 €";
    private static final String EXPECTED_STATUS = "created";

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
    @DisplayName("Create reference order and verify its result")
    void createReferenceOrder()
            throws IOException {

        // Create Page Objects
        LoginPage login = new LoginPage(driver, wait, config.baseUrl);
        CatalogPage catalog = new CatalogPage(driver, wait);
        CartPage cart = new CartPage(driver, wait);
        CheckoutPage checkout = new CheckoutPage(driver, wait);
        OrderDetailPage order = new OrderDetailPage(driver, wait);

        try {
            // ARRANGE
            login.open();
            login.login(config.email, config.password);

            catalog.waitUntilReady();

            // ACT
            catalog.addProduct("KB-001", 1);
            catalog.addProduct("KB-001", 2);
            catalog.addProduct("MS-001", 3);

            cart.open();
            cart.selectDelivery("courier");
            cart.applyCoupon("STUDENT10");
            cart.proceedToCheckout();

            checkout.waitUntilReady();
            checkout.fillCustomer(
                    "Student Testovaci",
                    "student@example.test",
                    "Testovacia 12",
                    "Nitra",
                    "949 01"
            );
            checkout.acceptTerms();
            checkout.placeOrder();

            // RESULT
            String orderId = order.getOrderId();
            saveOrderId(orderId);

            String actualCents = order.getTotalCents();
            String actualVisibleTotal = order.getVisibleTotal();
            String actualStatus = order.getStatus();

            // ASSERT
            assertAll(
                    "Reference order",
                    () -> assertFalse(orderId.isBlank(), "Order UUID must be displayed."),
                    () -> assertEquals(EXPECTED_TOTAL_CENTS, actualCents, "Incorrect total in cents."),
                    () -> assertEquals(EXPECTED_VISIBLE_TOTAL, actualVisibleTotal, "Incorrect visible total."),
                    () -> assertEquals(EXPECTED_STATUS, actualStatus, "Incorrect order status.")
            );
        } catch (RuntimeException | AssertionError error) {
            saveFailureScreenshot();
            throw error;
        }
    }

    // Store UUID as test evidence
    private void saveOrderId(
            String orderId
    ) throws IOException {
        Path directory = Path.of("target", "artifacts");

        Files.createDirectories(directory);
        Files.writeString(directory.resolve("order-id.txt"), orderId);
    }

    // Store screenshot when test fail
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
            System.err.println("Screenshot could not be saved: " + error.getMessage());
        }
    }
}