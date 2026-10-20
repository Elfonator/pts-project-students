package sk.testlab.support;

import io.restassured.filter.Filter;
import io.restassured.response.Response;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/**
 * Evidence under target/artifacts/… — screenshots for GUI, HTTP snippets for API.
 * run.properties is a small key=value summary of the run (not the DB).
 */
public final class Artifacts {
    public final String runId = UUID.randomUUID().toString();
    private final Path directory;
    private final List<String> secrets;
    private final Properties metadata = new Properties();
    private final AtomicInteger httpCounter = new AtomicInteger();

    public Artifacts(int repetition, String password) throws IOException {
        this.secrets = secretsOf(password);
        directory = Path.of("target", "artifacts", "run-" + repetition + "-" + runId);
        Files.createDirectories(directory);
        put("started.utc", Instant.now().toString());
        put("repetition", repetition);
        System.out.println("Artifacts: " + directory.toAbsolutePath());
    }

    // API / non-GUI folder
    public Artifacts(String testName, String... secretsToRedact) throws IOException {
        this.secrets = secretsOf(secretsToRedact);
        String safe = testName.replaceAll("[^a-zA-Z0-9_-]", "_");
        directory = Path.of("target", "artifacts", safe + "-" + runId);
        Files.createDirectories(directory);
        put("test", testName);
        put("started.utc", Instant.now().toString());
        System.out.println("Artifacts: " + directory.toAbsolutePath());
    }

    public Path directory() {
        return directory;
    }

    private static List<String> secretsOf(String... values) {
        List<String> list = new ArrayList<>();
        if (values == null) return list;
        for (String value : values) {
            if (value != null && !value.isBlank()) list.add(value);
        }
        return list;
    }

    private String redact(String text) {
        String result = text;
        for (String secret : secrets) {
            result = result.replace(secret, "[REDACTED]");
        }
        return result;
    }

    public void write(String name, String text) throws IOException {
        Files.writeString(directory.resolve(name), redact(text), StandardCharsets.UTF_8);
    }

    public void put(String key, Object value) throws IOException {
        metadata.setProperty(key, redact(String.valueOf(value)));
        try (Writer writer = Files.newBufferedWriter(
                directory.resolve("run.properties"), StandardCharsets.UTF_8)) {
            metadata.store(writer, "TestLab Shop - test evidence summary");
        }
    }

    public void note(String key, Object value) {
        try {
            put(key, value);
        } catch (IOException e) {
            System.err.println("Artifact metadata could not be saved: " + key);
        }
    }

    // Partial HTTP log
    public Filter httpLog(String route) {
        return (request, responseSpec, context) -> {
            int number = httpCounter.incrementAndGet();
            String prefix = String.format(Locale.ROOT, "%02d", number);
            boolean sensitive = route.startsWith("/auth/") || route.startsWith("/testing/");
            StringBuilder requestLog = new StringBuilder();
            requestLog.append(request.getMethod()).append(' ').append(request.getURI()).append('\n');
            requestLog.append("route=").append(route).append('\n');
            requestLog.append("Authorization=")
                    .append(request.getHeaders().hasHeaderWithName("Authorization") ? "[REDACTED]" : "absent")
                    .append('\n');
            requestLog.append("X-Test-Key=")
                    .append(request.getHeaders().hasHeaderWithName("X-Test-Key") ? "[REDACTED]" : "absent")
                    .append('\n');
            requestLog.append("body=\n")
                    .append(sensitive ? "[SENSITIVE BODY OMITTED]\n" : String.valueOf(request.getBody()));
            attempt(() -> write(prefix + "-request.txt", requestLog.toString()), null);

            Response response = context.next(request, responseSpec);

            StringBuilder responseLog = new StringBuilder();
            responseLog.append("status=").append(response.statusCode()).append('\n');
            responseLog.append("contentType=").append(response.contentType()).append('\n');
            responseLog.append("body=\n")
                    .append(sensitive ? "[SENSITIVE BODY OMITTED]\n" : response.asPrettyString());
            attempt(() -> write(prefix + "-response.txt", responseLog.toString()), null);
            note("last.route", route);
            note("last.status", response.statusCode());
            return response;
        };
    }

    public void capture(WebDriver driver, String prefix, Throwable failure) {
        if (failure != null) {
            StringWriter text = new StringWriter();
            failure.printStackTrace(new PrintWriter(text));
            attempt(() -> write(prefix + ".txt", text.toString()), failure);
        }
        if (driver == null) {
            attempt(() -> write(prefix + "-unavailable.txt",
                    "Browser session was not created."), failure);
            return;
        }
        attempt(() -> Files.write(directory.resolve(prefix + ".png"),
                ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES)), failure);
        attempt(() -> write(prefix + "-url.txt", driver.getCurrentUrl()), failure);

        // Read a DOM clone. Do not mutate the app; mask passwords and hidden values.
        attempt(() -> {
            String html = String.valueOf(((JavascriptExecutor) driver).executeScript("""
                const root = document.documentElement.cloneNode(true);
                root.querySelectorAll('input[type="password"], input[type="hidden"]')
                    .forEach(e => e.setAttribute('value', '[REDACTED]'));
                return '<!doctype html>\\n' + root.outerHTML;
                """));
            write(prefix + ".html", html);
        }, failure);
    }

    private void attempt(CheckedAction action, Throwable primary) {
        try {
            action.run();
        } catch (Exception problem) {
            if (primary != null) {
                primary.addSuppressed(problem);
            }
            System.err.println("Artifact could not be saved: " + redact(problem.toString()));
        }
    }

    @FunctionalInterface
    private interface CheckedAction {
        void run() throws Exception;
    }
}
