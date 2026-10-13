package sk.testlab.cvicenie02.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CheckoutPage {
    private final WebDriverWait wait;
    private final By checkoutForm = By.cssSelector("[data-testid='checkout-form']");
    private final By customerName = By.cssSelector("[data-testid='customer-name']");
    private final By customerEmail = By.cssSelector("[data-testid='customer-email']");
    private final By customerAddress = By.cssSelector("[data-testid='customer-address']");
    private final By customerCity = By.cssSelector("[data-testid='customer-city']");
    private final By customerPostalCode = By.cssSelector("[data-testid='customer-postal-code']");
    private final By acceptTerms = By.cssSelector("[data-testid='accept-terms']");
    private final By placeOrder = By.cssSelector("[data-testid='place-order']");

    public CheckoutPage(
            WebDriver driver,
            WebDriverWait wait
    ) {
        this.wait = wait;
    }

    public void waitUntilReady() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(checkoutForm));
    }

    public void fillCustomer(
            String name,
            String email,
            String address,
            String city,
            String postalCode
    ) {
        type(customerName, name);
        type(customerEmail, email);
        type(customerAddress, address);
        type(customerCity, city);
        type(customerPostalCode, postalCode);
    }

    public void acceptTerms() {
        wait.until(ExpectedConditions.elementToBeClickable(acceptTerms)).click();
    }

    public void placeOrder() {
        wait.until(ExpectedConditions.elementToBeClickable(placeOrder)).click();
    }

    private void type(
            By locator,
            String value
    ) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(locator));
        input.clear();
        input.sendKeys(value);
    }
}