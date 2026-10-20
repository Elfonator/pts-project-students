package sk.testlab.gui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.api.ApiFixtureHelper;
import sk.testlab.api.ApiOrderFixture;
import sk.testlab.gui.pages.LoginPage;
import sk.testlab.gui.pages.OrderCancelPage;
import sk.testlab.gui.pages.OrderDetailPage;
import sk.testlab.support.BrowserFactory;
import sk.testlab.support.TestConfig;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Optional stretch: Arrange by API, Act through real GUI, Assert in GUI. */
class ApiPreparedCancelOrderTest {
    private WebDriver driver;
    private TestConfig config;
    private ApiFixtureHelper.Workspace workspace;
    private String bearer;
    private String orderId;

    @Test
    void guiCancelsTheOrderPreparedByApi() {
        config = TestConfig.load();
        workspace = ApiFixtureHelper.acquire(config);
        bearer = ApiFixtureHelper.login(config, workspace);
        orderId = ApiOrderFixture.createReferenceOrder(config, bearer);

        driver = BrowserFactory.create(config);
        WebDriverWait wait = new WebDriverWait(driver, config.timeout);
        LoginPage login = new LoginPage(driver, wait, config.baseUrl);
        OrderDetailPage detail = new OrderDetailPage(driver, wait);
        OrderCancelPage cancellation = new OrderCancelPage(driver, wait);

        login.open();
        login.login(workspace.email(), workspace.password());
        driver.get(config.baseUrl + "#/orders/" + orderId);
        assertAll("API-prepared order before GUI action",
                () -> assertEquals(orderId, detail.getOrderId()),
                () -> assertEquals("8563", detail.getTotalCents()),
                () -> assertEquals("created", detail.getStatus()));

        // This is the tested GUI action. DO NOT cancel via API here.
        cancellation.cancelAndWaitUntilCancelled();
        assertAll("GUI cancellation",
                () -> assertEquals(orderId, detail.getOrderId()),
                () -> assertEquals("cancelled", detail.getStatus()));
    }

    @AfterEach
    void cleanup() {
        // API clean-up is only a fallback. It cannot make a failed GUI assertion pass.
        try {
            if (config != null && bearer != null && orderId != null) {
                ApiOrderFixture.cancelOrder(config, bearer, orderId);
            }
        } catch (Exception e) {
            System.err.println("Order cleanup failed: " + e.getClass().getSimpleName());
        }
        try {
            if (config != null && workspace != null) {
                ApiFixtureHelper.release(config, workspace);
            }
        } catch (Exception e) {
            System.err.println("Fixture cleanup failed: " + e.getClass().getSimpleName());
        } finally {
            if (driver != null) driver.quit();
        }
    }
}
