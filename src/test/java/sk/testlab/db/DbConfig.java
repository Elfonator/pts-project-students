package sk.testlab.db;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * JDBC settings only. Kept separate from {@code TestConfig} so GUI/API tests
 * do not require a database account.
 */
public final class DbConfig {
    public final String url;
    public final String user;
    public final String password;

    public static DbConfig load() {
        return from(System.getProperties(), System.getenv(), loadProperties());
    }

    public static DbConfig from(Properties system, Map<String, String> env, Properties local) {
        String url = required(value(system, env, local, "db.url", "DB_URL", ""), "db.url / DB_URL");
        validateJdbcUrl(url);
        String user = required(value(system, env, local, "db.user", "DB_USER", ""), "db.user / DB_USER");
        String password = requiredSecret(system, env, local, "db.password", "DB_PASSWORD");
        return new DbConfig(url, user, password);
    }

    private DbConfig(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private static Properties loadProperties() {
        Properties local = new Properties();
        Path path = Path.of("config", "test.properties");
        if (!Files.isRegularFile(path)) {
            return local;
        }
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            local.load(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load " + path.toAbsolutePath(), e);
        }
        return local;
    }

    private static String value(
            Properties system, Map<String, String> env, Properties local,
            String key, String envKey, String fallback) {
        if (system.containsKey(key)) return system.getProperty(key);
        if (env.containsKey(envKey)) return env.get(envKey);
        return local.getProperty(key, fallback);
    }

    private static String required(String input, String name) {
        if (input == null || input.isBlank() || input.stripLeading().startsWith("SEM_")) {
            throw new IllegalArgumentException(
                    "Fill " + name + " in config/test.properties (see database/create-readonly-user.sql).");
        }
        return input.trim();
    }

    private static String requiredSecret(
            Properties system, Map<String, String> env, Properties local,
            String key, String envKey) {
        String systemValue = system.getProperty(key);
        String envValue = env.get(envKey);
        String text = systemValue != null ? systemValue
                : envValue != null ? envValue
                : local.getProperty(key, "");
        if (text.isBlank() || text.stripLeading().startsWith("SEM_")) {
            throw new IllegalArgumentException(
                    "Fill " + key + " / " + envKey + " (read-only DB password).");
        }
        return text;
    }

    private static void validateJdbcUrl(String value) {
        if (!value.startsWith("jdbc:mariadb://")) {
            throw new IllegalArgumentException(
                    "db.url must look like jdbc:mariadb://host:port/database");
        }
        URI uri = URI.create(value.substring("jdbc:".length()));
        if (uri.getRawAuthority() == null || uri.getRawAuthority().isBlank()
                || uri.getPath() == null || uri.getPath().length() <= 1
                || uri.getUserInfo() != null) {
            throw new IllegalArgumentException(
                    "db.url needs host + database and must not embed credentials.");
        }
        String query = uri.getRawQuery();
        if (query != null) {
            String lower = query.toLowerCase(Locale.ROOT);
            if (lower.contains("password=") || lower.contains("user=")) {
                throw new IllegalArgumentException(
                        "Put credentials in db.user / db.password, not in db.url.");
            }
        }
    }

    @Override
    public String toString() {
        return "DbConfig[url=" + url + ", user=" + user + ", password=REDACTED]";
    }
}
