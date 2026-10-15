package sk.testlab.workflows;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.gui.pages.*;
import sk.testlab.support.TestConfig;

// Coordinate a multi-page GUI workflow - business assertions belong in tests
public class OrderWorkflow {
    private final LoginPage login;
    private final CatalogPage catalog;
    private final CartPage cart;
    private final CheckoutPage checkout;
    private final OrderDetailPage order;

    public OrderWorkflow(WebDriver driver, WebDriverWait wait, TestConfig config) {
        login = new LoginPage(driver, wait, config.baseUrl);
        catalog = new CatalogPage(driver, wait);
        cart = new CartPage(driver, wait);
        checkout = new CheckoutPage(driver, wait);
        order = new OrderDetailPage(driver, wait);
    }

    // Create an order with the same inputs as CreateOrderWithPOMTest
    public String createReferenceOrder(String email, String password) {
        login.open();
        login.login(email, password);
        catalog.waitUntilReady();

        catalog.addProduct("KB-001", 1);
        catalog.addProduct("KB-001", 2);
        catalog.addProduct("MS-001", 3);

        cart.open();
        cart.selectDelivery("courier");
        cart.applyCoupon("STUDENT10");
        cart.proceedToCheckout();

        checkout.waitUntilReady();
        checkout.fillCustomer(
                "Student Testovaci", "student@example.test", "Testovacia 12",
                "Nitra", "949 01");
        checkout.acceptTerms();
        checkout.placeOrder();

        // Return the UUID to identify this particular order
        return order.getOrderId();
    }
}
