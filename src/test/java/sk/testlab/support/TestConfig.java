package sk.testlab.support;

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
import java.util.Set;

/** Konfigurácia infraštruktúry; neobsahuje testovacie kroky ani očakávané ceny */
public final class TestConfig {
    public final String baseUrl;
    public final String email;
    public final String password;
    public final String workspace;
    // api = create/delete run-* via test API; manual = use test.email/password
    public final String fixturesMode;
    // Empty when fixtures.mode=manual. At least 32 characters when fixtures.mode=api
    public final String testApiKey;
    public final boolean allowHttp;
    public final String browser;
    public final boolean headless;
    public final Duration timeout;
    public final boolean cleanupOrder;
    public final String declaredAppVersion;
    public final String driverPath;
    public final String browserBinary;

    public static TestConfig load() {
        return from(System.getProperties(), System.getenv(), loadProperties());
    }

    /** Načíta konfiguráciu pre zvolené opakovanie testu. */
    public static TestConfig loadForRun(int runNumber) {
        if (runNumber < 1) {
            throw new IllegalArgumentException(
                    "Run number must be positive."
            );
        }

        return new TestConfig(
                System.getProperties(),
                System.getenv(),
                loadProperties(),
                "run." + runNumber + "."
        );
    }

    private static Properties loadProperties() {
        Properties local = new Properties();
        Path path = Path.of("config", "test.properties");
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                local.load(reader);
            } catch (IOException e) {
                throw new UncheckedIOException("Nepodarilo sa načítať " + path.toAbsolutePath(), e);
            }
        }
        return local;
    }

    /** Oddelené vstupy umožňujú kontrolovať konfiguráciu bez Selenium a bez servera. */
    public static TestConfig from(Properties system, Map<String, String> environment, Properties local) {
        return new TestConfig(system, environment, local);
    }

    private TestConfig(Properties system, Map<String, String> env, Properties local) {
        this(system, env, local, null);
    }

    private TestConfig(Properties system, Map<String, String> env, Properties local, String runPrefix) {
        baseUrl = normalizeBaseUrl(value(system, env, local, "base.url", "BASE_URL", "http://127.0.0.1:8080/"));
        fixturesMode = normalizeFixturesMode(value(system, env, local, "fixtures.mode", "FIXTURES_MODE", "manual"));
        allowHttp = bool(value(system, env, local, "allow.http", "ALLOW_HTTP", "true"), "allow.http");
        if (baseUrl.startsWith("http://") && !allowHttp) {
            throw new IllegalArgumentException(
                    "HTTP posiela prihlasovacie údaje bez šifrovania. "
                            + "Pre lokálny Docker nastav allow.http=true, inak použi HTTPS.");
        }

        // Prefer env TEST_API_KEY over the properties file (same idea as Dominik's project).
        String keyFromEnv = env.get("TEST_API_KEY");
        if (keyFromEnv != null && !keyFromEnv.isBlank() && !keyFromEnv.stripLeading().startsWith("SEM_")) {
            testApiKey = normalizeTestApiKey(keyFromEnv);
        } else {
            testApiKey = normalizeTestApiKey(value(system, env, local, "test.api.key", "TEST_API_KEY", ""));
        }

        if (automaticFixtures() && testApiKey.length() < 32) {
            throw new IllegalArgumentException(
                    "fixtures.mode=api vyžaduje test.api.key alebo TEST_API_KEY "
                            + "(aspoň 32 znakov, zhodný s Docker TEST_API_KEY).");
        }

        if (runPrefix == null) {
            if (automaticFixtures()) {
                // API tests create their own run-* account; email/password stay optional for GUI.
                email = optionalCredential(value(system, env, local, "test.email", "TEST_EMAIL", "")).trim();
                password = optionalCredential(value(system, env, local, "test.password", "TEST_PASSWORD", ""));
                workspace = optionalCredential(value(system, env, local, "test.workspace", "TEST_WORKSPACE", "")).trim();
            } else {
                email = required(value(system, env, local, "test.email", "TEST_EMAIL", ""), "test.email / TEST_EMAIL").trim();
                // Heslo sa nesmie trimovať: medzera môže byť jeho platnou súčasťou.
                password = required(value(system, env, local, "test.password", "TEST_PASSWORD", ""), "test.password / TEST_PASSWORD");
                // Voliteľné pre kompatibilitu so staršími konfiguráciami.
                workspace = value(system, env, local, "test.workspace", "TEST_WORKSPACE", "").trim();
            }
        } else {
            email = required(local.getProperty(runPrefix + "email"), runPrefix + "email").trim();
            password = required(local.getProperty(runPrefix + "password"), runPrefix + "password");
            workspace = required(local.getProperty(runPrefix + "workspace"), runPrefix + "workspace").trim();
        }
        browser = value(system, env, local, "browser", "BROWSER", "chrome").trim().toLowerCase(Locale.ROOT);
        if (!Set.of("chrome", "edge", "firefox").contains(browser)) {
            throw new IllegalArgumentException("browser musí byť chrome, edge alebo firefox.");
        }
        headless = bool(value(system, env, local, "headless", "HEADLESS", "false"), "headless");
        cleanupOrder = bool(value(system, env, local, "cleanup.order", "CLEANUP_ORDER", "true"), "cleanup.order");
        int seconds;
        try {
            seconds = Integer.parseInt(value(system, env, local, "timeout.seconds", "TIMEOUT_SECONDS", "15").trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("timeout.seconds musí byť celé číslo 1 až 120.", e);
        }
        if (seconds < 1 || seconds > 120) throw new IllegalArgumentException("timeout.seconds musí byť 1 až 120.");
        timeout = Duration.ofSeconds(seconds);
        declaredAppVersion = value(system, env, local, "app.version", "APP_VERSION", "NEZADANÁ").trim();
        driverPath = value(system, env, local, "driver.path", "DRIVER_PATH", "").trim();
        browserBinary = value(system, env, local, "browser.binary", "BROWSER_BINARY", "").trim();
    }

    private static String value(Properties system, Map<String, String> env, Properties local,
                                String key, String envKey, String fallback) {
        if (system.containsKey(key)) return system.getProperty(key);
        if (env.containsKey(envKey)) return env.get(envKey);
        return local.getProperty(key, fallback);
    }

    public boolean automaticFixtures() {
        return "api".equals(fixturesMode);
    }

    public void requireTestApiKey() {
        if (!automaticFixtures() || testApiKey.length() < 32) {
            throw new IllegalStateException(
                    "Automatické fixtures vyžadujú fixtures.mode=api, test.api.key (≥32) "
                            + "a Docker APP_ENV=testing, ENABLE_TEST_API=true, TEST_API_KEY=rovnaký kľúč.");
        }
    }

    private static String normalizeFixturesMode(String input) {
        String mode = input == null ? "manual" : input.trim().toLowerCase(Locale.ROOT);
        if (!Set.of("api", "manual").contains(mode)) {
            throw new IllegalArgumentException("fixtures.mode musí byť api alebo manual.");
        }
        return mode;
    }

    private static String normalizeTestApiKey(String input) {
        if (input == null) return "";
        String key = input.trim();
        if (key.isEmpty() || key.stripLeading().startsWith("SEM_")) return "";
        return key;
    }

    private static String optionalCredential(String input) {
        if (input == null || input.isBlank() || input.stripLeading().startsWith("SEM_")) return "";
        return input;
    }

    private static String required(String input, String name) {
        if (input == null || input.isBlank() || input.stripLeading().startsWith("SEM_")) {
            throw new IllegalArgumentException("Doplňte " + name
                    + ". Skopírujte config/test.example.properties do config/test.properties"
                    + " a použite účet pracovného priestoru z fixtures,"
                    + " alebo nastavte fixtures.mode=api a test.api.key.");
        }
        return input;
    }

    private static boolean bool(String input, String key) {
        if ("true".equalsIgnoreCase(input.trim())) return true;
        if ("false".equalsIgnoreCase(input.trim())) return false;
        throw new IllegalArgumentException(key + " musí mať hodnotu true alebo false.");
    }

    public static String normalizeBaseUrl(String input) {
        if (input == null || input.isBlank()) throw new IllegalArgumentException("base.url nesmie byť prázdna.");
        URI uri;
        try { uri = URI.create(input.trim()); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("Neplatná base.url.", e); }
        String scheme = uri.getScheme();
        if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                || uri.getHost() == null || uri.getUserInfo() != null
                || uri.getRawQuery() != null || uri.getRawFragment() != null
                || uri.getPort() > 65535) {
            throw new IllegalArgumentException("base.url musí byť HTTP(S) adresa webu bez hesla, query a # fragmentu.");
        }
        String path = uri.getPath() == null ? "" : uri.getPath();
        if (path.endsWith("/api/index.php") || path.endsWith("/api") || path.endsWith("/api/")) {
            throw new IllegalArgumentException("Zadajte adresu WEBU, nie API endpoint.");
        }
        String result = uri.toASCIIString();
        if (path.endsWith("/index.php")) result = result.substring(0, result.length() - "index.php".length());
        return result.endsWith("/") ? result : result + "/";
    }
}
