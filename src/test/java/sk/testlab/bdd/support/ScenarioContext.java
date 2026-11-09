package sk.testlab.bdd.support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.gui.pages.CartPage;
import sk.testlab.gui.pages.CatalogPage;
import sk.testlab.gui.pages.CheckoutPage;
import sk.testlab.gui.pages.LoginPage;
import sk.testlab.gui.pages.OrderDetailPage;
import sk.testlab.support.TestConfig;

/** Per-scenario browser and page objects shared by step definitions. */
public final class ScenarioContext {
    private static final ThreadLocal<ScenarioContext> CURRENT = new ThreadLocal<>();

    public TestConfig config;
    public WebDriver driver;
    public WebDriverWait wait;

    public LoginPage login;
    public CatalogPage catalog;
    public CartPage cart;
    public CheckoutPage checkout;
    public OrderDetailPage order;

    public static ScenarioContext get() {
        ScenarioContext context = CURRENT.get();
        if (context == null) {
            throw new IllegalStateException("ScenarioContext is not initialized. Check Hooks.");
        }
        return context;
    }

    static void set(ScenarioContext context) {
        CURRENT.set(context);
    }

    static void clear() {
        CURRENT.remove();
    }
}
