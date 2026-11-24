package sk.testlab.mobile;

import io.appium.java_client.android.AndroidDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import sk.testlab.mobile.support.MobileUi;
import sk.testlab.support.TestConfig;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * EXAMPLE
 * Uses shared {@link MobileUi} helpers -> same idea as Selenium {@code CreateOrderTest}.
 * Students later put catalog steps into {@link sk.testlab.mobile.screens.CatalogScreen}.
 */
@Tag("mobile")
@Disabled("Enable when Appium + emulator + APK/app are ready")
class MobileLoginCatalogExampleTest {
    private static final String KEYBOARD_SKU = "KB-001";
    // Seed unit price 3490 cents → Slovak currency text from the app
    private static final String EXPECTED_KB_PRICE_FRAGMENT = "34,90";

    private MobileConfig mobile;
    private TestConfig web;
    private AndroidDriver driver;
    private WebDriverWait wait;
    private MobileUi ui;

    @BeforeEach
    void setUp() {
        mobile = MobileConfig.load();
        web = TestConfig.load();
        driver = AndroidDriverFactory.create(mobile);
        wait = new WebDriverWait(driver, mobile.timeout);
        ui = new MobileUi(driver, wait);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void openAccount() {
        ui.click("nav_account");
    }

    private void login(String email, String password) {
        ui.type("login_email", email);
        ui.type("login_password", password);
        ui.click("login_submit");
        // Successful login finishes this screen and opens the catalog.
        ui.textContaining("current_user", email);
    }

    private void openCatalog() {
        ui.click("nav_catalog");
        ui.waitUntilGone("loading_overlay");
        ui.visible("catalog_ready");
    }

    private void searchCatalog(String query) {
        ui.type("search_input", query);
        ui.click("search_submit");
        ui.waitUntilGone("loading_overlay");
        ui.visible("catalog_ready");
    }

    private String productPrice(String skuSuffix) {
        return ui.visible("product_price_" + skuSuffix).getText();
    }

    private static String normalizePrice(String text) {
        return text.replace('\u00a0', ' ');
    }

    @Test
    @DisplayName("EXAMPLE: mobile login + KB-001 catalog price")
    void loginAndAssertKeyboardPrice() {
        openAccount();
        login(web.email, web.password);

        openCatalog();
        searchCatalog(KEYBOARD_SKU);
        ui.visible("product_card_kb_001");

        String price = normalizePrice(productPrice("kb_001"));
        assertTrue(
                price.contains(EXPECTED_KB_PRICE_FRAGMENT),
                () -> "Expected KB-001 price to contain " + EXPECTED_KB_PRICE_FRAGMENT + " but was: " + price
        );
    }
}
