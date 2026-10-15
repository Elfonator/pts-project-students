package sk.testlab.gui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CartPage {
    private final WebDriverWait wait;
    private final By navCart = By.cssSelector("[data-testid='nav-cart']");
    private final By deliveryMethod = By.cssSelector("[data-testid='delivery-method']");
    private final By couponInput = By.cssSelector("[data-testid='coupon-input']");
    private final By couponApply = By.cssSelector("[data-testid='coupon-apply']");
    private final By quoteTotal = By.cssSelector("[data-testid='quote-total']");
    private final By proceedCheckout = By.cssSelector("[data-testid='proceed-checkout']");

    public CartPage(
            WebDriver driver,
            WebDriverWait wait
    ) {
        this.wait = wait;
    }

    public void open() {
        wait.until(ExpectedConditions.elementToBeClickable(navCart)).click();
    }

    public void selectDelivery(String value) {
        WebElement select = wait.until(ExpectedConditions.visibilityOfElementLocated(deliveryMethod));
        new Select(select).selectByValue(value);
    }

    public void applyCoupon(String coupon) {
        // Remember old price element
        WebElement previousQuote = wait.until(ExpectedConditions.visibilityOfElementLocated(quoteTotal));
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(couponInput));

        input.clear();
        input.sendKeys(coupon);

        wait.until(ExpectedConditions.elementToBeClickable(couponApply)).click();

        // Wait until Vue replaces the old price
        wait.until(ExpectedConditions.stalenessOf(previousQuote));

        // Wait for the new price
        wait.until(ExpectedConditions.visibilityOfElementLocated(quoteTotal));
    }

    public void proceedToCheckout() {
        wait.until(ExpectedConditions.elementToBeClickable(proceedCheckout)).click();
    }
}