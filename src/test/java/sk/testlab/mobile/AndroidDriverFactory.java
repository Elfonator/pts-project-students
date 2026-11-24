package sk.testlab.mobile;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;

import java.net.MalformedURLException;
import java.net.URL;

// Creates an Appium AndroidDriver session (UiAutomator2)
public final class AndroidDriverFactory {
    private AndroidDriverFactory() { }

    public static AndroidDriver create(MobileConfig config) {
        UiAutomator2Options options = new UiAutomator2Options()
                .setUdid(config.deviceUdid)
                .setNoReset(config.noReset);

        if (!config.apkPath.isBlank()) {
            options.setApp(config.apkPath);
        } else {
            options.setAppPackage(config.appPackage);
            options.setAppActivity(config.appActivity);
        }

        try {
            URL server = config.appiumUrl.toURL();
            return new AndroidDriver(server, options);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid appium.url: " + config.appiumUrl, e);
        }
    }
}
