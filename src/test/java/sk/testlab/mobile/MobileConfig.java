package sk.testlab.mobile;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

// Appium / Android session settings. Separate from web {@code TestConfig}
public final class MobileConfig {
    public final URI appiumUrl;
    public final String deviceUdid;
    // Absolute APK path; empty means start by package/activity only
    public final String apkPath;
    public final String appPackage;
    public final String appActivity;
    public final boolean noReset;
    public final Duration timeout;

    public static MobileConfig load() {
        return from(System.getProperties(), System.getenv(), loadProperties());
    }

    public static MobileConfig from(Properties system, Map<String, String> env, Properties local) {
        return new MobileConfig(system, env, local);
    }

    private MobileConfig(Properties system, Map<String, String> env, Properties local) {
        appiumUrl = URI.create(value(system, env, local, "appium.url", "APPIUM_URL", "http://127.0.0.1:4723").trim());
        deviceUdid = value(system, env, local, "device.udid", "DEVICE_UDID", "emulator-5554").trim();
        apkPath = value(system, env, local, "apk.path", "APK_PATH", "").trim();
        appPackage = value(system, env, local, "app.package", "APP_PACKAGE", "sk.testlab.shop").trim();
        appActivity = value(system, env, local, "app.activity", "APP_ACTIVITY", "sk.testlab.shop.ui.MainActivity").trim();
        noReset = bool(value(system, env, local, "appium.noReset", "APPIUM_NO_RESET", "false"));
        int seconds;
        try {
            seconds = Integer.parseInt(value(system, env, local, "timeout.seconds", "TIMEOUT_SECONDS", "30").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("timeout.seconds must be an integer 1..120.", e);
        }
        if (seconds < 1 || seconds > 120) {
            throw new IllegalArgumentException("timeout.seconds must be 1..120.");
        }
        timeout = Duration.ofSeconds(seconds);

        if (deviceUdid.isBlank()) {
            throw new IllegalArgumentException("Set device.udid or DEVICE_UDID (see: adb devices).");
        }
        if (apkPath.isBlank() && (appPackage.isBlank() || appActivity.isBlank())) {
            throw new IllegalArgumentException(
                    "Set apk.path / APK_PATH, or both app.package and app.activity for an installed app.");
        }
        if (!apkPath.isBlank() && !Files.isRegularFile(Path.of(apkPath))) {
            throw new IllegalArgumentException("apk.path does not point to a file: " + apkPath);
        }
    }

    private static Properties loadProperties() {
        Properties local = new Properties();
        Path path = Path.of("config", "test.properties");
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                local.load(reader);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to read " + path.toAbsolutePath(), e);
            }
        }
        return local;
    }

    private static String value(Properties system, Map<String, String> env, Properties local,
                                String key, String envKey, String fallback) {
        if (system.containsKey(key)) return system.getProperty(key);
        if (env.containsKey(envKey)) return env.get(envKey);
        return local.getProperty(key, fallback);
    }

    private static boolean bool(String input) {
        String v = input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
        if ("true".equals(v)) return true;
        if ("false".equals(v)) return false;
        throw new IllegalArgumentException("appium.noReset must be true or false.");
    }
}
