package sk.testlab.gui.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String baseUrl;

    private final By navLogin = By.cssSelector("[data-testid='nav-login']");
    private final By email = By.cssSelector("[data-testid='login-email']");
    private final By password = By.cssSelector("[data-testid='login-password']");
    private final By submit = By.cssSelector("[data-testid='login-submit']");
    private final By currentUser = By.cssSelector("[data-testid='current-user']");

    public LoginPage(
            WebDriver driver,
            WebDriverWait wait,
            String baseUrl
    ) {
        this.driver = driver;
        this.wait = wait;
        this.baseUrl = baseUrl;
    }

    public void open() {driver.get(baseUrl);}

    public void login(
            String userEmail,
            String userPassword
    ) {
        click(navLogin);
        type(email, userEmail);
        type(password, userPassword);
        click(submit);
        wait.until(ExpectedConditions.visibilityOfElementLocated(currentUser));
    }

    private void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    private void type(By locator, String value) {
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(locator));
        input.clear();
        input.sendKeys(value);
    }
}