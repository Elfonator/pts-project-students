package sk.testlab.bdd.support;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.gui.pages.CartPage;
import sk.testlab.gui.pages.CatalogPage;
import sk.testlab.gui.pages.CheckoutPage;
import sk.testlab.gui.pages.LoginPage;
import sk.testlab.gui.pages.OrderDetailPage;
import sk.testlab.support.BrowserFactory;
import sk.testlab.support.TestConfig;

public class Hooks {

    @Before
    public void setUp() {
        ScenarioContext context = new ScenarioContext();
        context.config = TestConfig.load();
        context.driver = BrowserFactory.create(context.config);
        context.wait = new WebDriverWait(context.driver, context.config.timeout);

        context.login = new LoginPage(context.driver, context.wait, context.config.baseUrl);
        context.catalog = new CatalogPage(context.driver, context.wait);
        context.cart = new CartPage(context.driver, context.wait);
        context.checkout = new CheckoutPage(context.driver, context.wait);
        context.order = new OrderDetailPage(context.driver, context.wait);

        ScenarioContext.set(context);
    }

    @After
    public void tearDown() {
        ScenarioContext context = ScenarioContext.get();
        if (context.driver != null) {
            context.driver.quit();
        }
        ScenarioContext.clear();
    }
}
