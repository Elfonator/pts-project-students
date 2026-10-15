package sk.testlab.gui;

import java.io.IOException;

import org.junit.jupiter.api.*;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import sk.testlab.gui.pages.CartPage;
import sk.testlab.gui.pages.CatalogPage;
import sk.testlab.gui.pages.CheckoutPage;
import sk.testlab.gui.pages.LoginPage;
import sk.testlab.gui.pages.OrderDetailPage;

import sk.testlab.support.Artifacts;
import sk.testlab.support.BrowserFactory;
import sk.testlab.support.TestConfig;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class StableCreateOrderTest {
    private static final String EXPECTED_TOTAL_CENTS = "8563";
    private static final String EXPECTED_VISIBLE_TOTAL = "85,63 €";
    private static final String EXPECTED_STATUS = "created";

    private WebDriver driver;
    private WebDriverWait wait;
    private TestConfig config;
    private Artifacts artifacts;

    @BeforeEach
    void setUp(RepetitionInfo repetition) throws IOException {
        //NEW CONFIG LOAD
        config = TestConfig.loadForRun(repetition.getCurrentRepetition());
        //config = TestConfig.load();
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

    //NEW TEST ANNOTATION
    @RepeatedTest(3)
    //@Test
    @DisplayName("Create reference order and verify its result")
    void createReferenceOrder() throws IOException {
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
            artifacts.write("order-id.txt", orderId);
            artifacts.put("order.id", orderId);

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
            // Capture before tearDown / driver.quit()
            artifacts.capture(driver, "failure", error);
            throw error;
        }
    }
}
