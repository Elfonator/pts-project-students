package sk.testlab.mobile;

import io.appium.java_client.android.AndroidDriver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.support.ui.WebDriverWait;

import sk.testlab.mobile.screens.CatalogScreen;
import sk.testlab.mobile.screens.LoginScreen;
import sk.testlab.support.TestConfig;

/**
 * HOMEWORK / class task — finish {@link CatalogScreen}, then complete this smoke test.
 * <p>
 * First study the EXAMPLE {@link MobileLoginCatalogExampleTest}.
 * Reuse {@link LoginScreen} (already done). Implement catalog steps only in {@link CatalogScreen}.
 * <p>
 * Expected: after login, search KB-001 and assert its catalog price (seed: 3490 cents → contains {@code 34,90}).
 */
@Tag("mobile")
@Disabled("Enable when Appium + emulator + APK/app are ready; finish CatalogScreen TODOs first")
class MobileLoginCatalogTaskTest {
    private static final String KEYBOARD_SKU = "KB-001";
    private static final String EXPECTED_KB_PRICE_FRAGMENT = "34,90";

    private MobileConfig mobile;
    private TestConfig web;
    private AndroidDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        mobile = MobileConfig.load();
        web = TestConfig.load();
        driver = AndroidDriverFactory.create(mobile);
        wait = new WebDriverWait(driver, mobile.timeout);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("HOMEWORK: mobile login + catalog price via CatalogScreen")
    void loginSearchAndAssertKeyboardPrice() {
        LoginScreen login = new LoginScreen(driver, wait);
        CatalogScreen catalog = new CatalogScreen(driver, wait);

        // given — login is prepared (same as EXAMPLE)
        login.openAccountTab();
        login.login(web.email, web.password);
        login.waitUntilSignedIn();

        // TODO A: open catalog tab and wait until ready

        // TODO B: search for KEYBOARD_SKU

        // TODO C: read catalog.productPrice(KEYBOARD_SKU)
        //         normalize NBSP if needed, assert it contains EXPECTED_KB_PRICE_FRAGMENT

        // TODO D (optional stretch): also assert product_stock_kb_001 is visible / non-empty
    }
}
