package sk.testlab.mobile.screens;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.mobile.support.MobileUi;

// Native login / account screen
public final class LoginScreen {
    private final MobileUi ui;

    public LoginScreen(AndroidDriver driver, WebDriverWait wait) {
        this.ui = new MobileUi(driver, wait);
    }

    public LoginScreen(MobileUi ui) {
        this.ui = ui;
    }

    public void openAccountTab() {
        ui.click("nav_account");
    }

    public void login(String email, String password) {
        ui.type("login_email", email);
        ui.type("login_password", password);
        ui.click("login_submit");
    }

    public void waitUntilSignedIn() {
        // Successful login finishes this screen and opens the catalog
        ui.textContaining("current_user", "@");
    }
}
