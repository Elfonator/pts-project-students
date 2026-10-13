package sk.testlab.cvicenie02.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.cvicenie02.support.TextChecks;

public class OrderDetailPage {
    private final WebDriverWait wait;
    private final By orderId = By.cssSelector("[data-testid='order-id']");
    private final By orderTotal = By.cssSelector("[data-testid='order-total']");
    private final By orderStatus = By.cssSelector("[data-testid='order-status']");

    public OrderDetailPage(
            WebDriver driver,
            WebDriverWait wait
    ) {
        this.wait = wait;
    }

    public String getOrderId() {
        // Succeeding of wait only when element is visible AND contains nonempty UUID
        return wait.until(driver -> {
            WebElement element = driver.findElement(orderId);
            if (!element.isDisplayed()) return null;

            String text = element.getText().trim();
            return text.isEmpty() ? null : text;
        });
    }

    public String getTotalCents() {
        return visible(orderTotal).getDomAttribute("data-cents");
    }

    public String getVisibleTotal() {
        String text = visible(orderTotal).getText();
        return TextChecks.normalizeSpaces(text);
    }

    public String getStatus() {
        return visible(orderStatus).getDomAttribute("data-status");
    }

    private WebElement visible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}