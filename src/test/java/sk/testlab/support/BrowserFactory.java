package sk.testlab.support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;

import java.nio.file.Files;
import java.nio.file.Path;

/** Nový dočasný profil pri každom teste. Nepoužíva osobný profil študenta. */
public final class BrowserFactory {
    private BrowserFactory() { }

    public static WebDriver create(TestConfig config) {
        if (!config.driverPath.isBlank() && !Files.isRegularFile(Path.of(config.driverPath))) {
            throw new IllegalArgumentException("driver.path neukazuje na súbor ovládača.");
        }
        if (!config.browserBinary.isBlank() && !Files.isRegularFile(Path.of(config.browserBinary))) {
            throw new IllegalArgumentException("browser.binary neukazuje na spustiteľný súbor prehliadača.");
        }
        return switch (config.browser) {
            case "edge" -> edge(config);
            case "firefox" -> firefox(config);
            default -> chrome(config);
        };
    }

    private static WebDriver chrome(TestConfig c) {
        ChromeOptions options = new ChromeOptions();
        if (c.headless) options.addArguments("--headless=new");
        options.addArguments("--window-size=1440,1000");
        if (!c.browserBinary.isBlank()) options.setBinary(c.browserBinary);
        if (c.driverPath.isBlank()) return new ChromeDriver(options); // Selenium Manager
        return new ChromeDriver(new ChromeDriverService.Builder()
                .usingDriverExecutable(Path.of(c.driverPath).toFile()).build(), options);
    }

    private static WebDriver edge(TestConfig c) {
        EdgeOptions options = new EdgeOptions();
        if (c.headless) options.addArguments("--headless=new");
        options.addArguments("--window-size=1440,1000");
        if (!c.browserBinary.isBlank()) options.setBinary(c.browserBinary);
        if (c.driverPath.isBlank()) return new EdgeDriver(options);
        return new EdgeDriver(new EdgeDriverService.Builder()
                .usingDriverExecutable(Path.of(c.driverPath).toFile()).build(), options);
    }

    private static WebDriver firefox(TestConfig c) {
        FirefoxOptions options = new FirefoxOptions();
        if (c.headless) options.addArguments("-headless");
        if (!c.browserBinary.isBlank()) options.setBinary(c.browserBinary);
        if (c.driverPath.isBlank()) return new FirefoxDriver(options);
        return new FirefoxDriver(new GeckoDriverService.Builder()
                .usingDriverExecutable(Path.of(c.driverPath).toFile()).build(), options);
    }
}