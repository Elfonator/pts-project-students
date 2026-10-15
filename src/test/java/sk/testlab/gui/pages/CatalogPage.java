package sk.testlab.gui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CatalogPage {

    private final WebDriverWait wait;
    private final By catalogReady = By.cssSelector("[data-testid='catalog-ready']");
    private final By cartCount = By.cssSelector("[data-testid='cart-count']");

    public CatalogPage(
            WebDriver driver,
            WebDriverWait wait
    ) {
        this.wait = wait;
    }

    public void waitUntilReady() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(catalogReady));
    }

    public void addProduct(
            String productCode,
            int expectedCartCount
    ) {
        By addButton = By.cssSelector("[data-testid='add-to-cart-" + productCode + "']");

        wait.until(ExpectedConditions.elementToBeClickable(addButton)).click();
        wait.until(ExpectedConditions.textToBe(cartCount, String.valueOf(expectedCartCount)));
    }
}