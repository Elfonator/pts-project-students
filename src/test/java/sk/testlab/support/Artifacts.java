package sk.testlab.support;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

/** Evidence is collected before cleanup and before driver.quit(). */
public final class Artifacts {
    public final String runId = UUID.randomUUID().toString();
    private final Path directory;
    private final String password;
    private final Properties metadata = new Properties();

    public Artifacts(int repetition, String password) throws IOException {
        this.password = password;
        directory = Path.of("target", "artifacts", "run-" + repetition + "-" + runId);
        Files.createDirectories(directory);
        put("started.utc", Instant.now().toString());
        put("repetition", repetition);
        System.out.println("Artifacts: " + directory.toAbsolutePath());
    }

    private String redact(String text) {
        return password == null || password.isEmpty()
                ? text : text.replace(password, "[REDACTED]");
    }

    public void write(String name, String text) throws IOException {
        Files.writeString(directory.resolve(name), redact(text), StandardCharsets.UTF_8);
    }

    public void put(String key, Object value) throws IOException {
        metadata.setProperty(key, redact(String.valueOf(value)));
        try (Writer writer = Files.newBufferedWriter(
                directory.resolve("run.properties"), StandardCharsets.UTF_8)) {
            metadata.store(writer, "TestLab Shop - exercise 03");
        }
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
