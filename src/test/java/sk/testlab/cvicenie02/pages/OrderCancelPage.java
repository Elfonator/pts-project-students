package sk.testlab.cvicenie02.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

// Cancellation actions on the order detail screen
public class OrderCancelPage {
    private static final By CANCEL_BUTTON = By.cssSelector("[data-testid='cancel-order']");
    private static final By CONFIRM_BUTTON = By.cssSelector("[data-testid='confirm-cancel']");
    private static final By ORDER_STATUS = By.cssSelector("[data-testid='order-status']");

    private final WebDriverWait wait;

    public OrderCancelPage(WebDriver driver, WebDriverWait wait) {
        this.wait = wait;
    }

    public void cancelAndWaitUntilCancelled() {
        wait.until(ExpectedConditions.elementToBeClickable(CANCEL_BUTTON)).click();
        wait.until(ExpectedConditions.elementToBeClickable(CONFIRM_BUTTON)).click();
        // Wait for the outcome rather than relying on an arbitrary sleep
        wait.until(ExpectedConditions.attributeToBe(
                ORDER_STATUS, "data-status", "cancelled"));
    }
}