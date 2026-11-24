package sk.testlab.mobile.support;

import io.appium.java_client.AppiumBy;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Small Appium helpers shared by screen objects and example tests
 * (same idea as private helpers in Selenium {@code CreateOrderTest}).
 */
public final class MobileUi {
    public static final String ID_PREFIX = "sk.testlab.shop:id/";

    private final AndroidDriver driver;
    private final WebDriverWait wait;

    public MobileUi(AndroidDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

    public By id(String name) {
        return AppiumBy.id(ID_PREFIX + name);
    }

    public WebElement visible(String name) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(id(name)));
    }

    public WebElement textContaining(String name, String fragment) {
        By locator = id(name);
        wait.until(ExpectedConditions.textToBePresentInElementLocated(locator, fragment));
        return driver.findElement(locator);
    }

    public void click(String name) {
        wait.until(ExpectedConditions.elementToBeClickable(id(name))).click();
    }

    public void type(String name, String value) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(id(name)));
        input.clear();
        input.sendKeys(value);
    }

    public void waitUntilGone(String name) {
        wait.until(ExpectedConditions.invisibilityOfElementLocated(id(name)));
    }
}
