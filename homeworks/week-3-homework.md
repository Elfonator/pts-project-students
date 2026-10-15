## Homework - Three fixtures and repeated GUI tests

### Goal
Explore the updated Selenium project and run the stable GUI tests using **three separate workspaces**.

### Steps

1. **Explore the new structure.** Find `gui/`, `gui/pages/`, `support/`, and `workflows/`. Open `StableCreateOrderTest` to see how repeated tests and `Artifacts` work.
2. **Create three fixtures** in your local web application (steps in `presentations/02_selenium_tests.pdf`).
3. **Update `config/test.properties`.** Copy `config/test.example.properties` if needed. Fill in `run.1.*`, `run.2.*`, and `run.3.*` using three different accounts/workspaces. Keep `test.email`, `test.password`, and `test.workspace` for ordinary tests.
4. **Run `StableCreateOrderTest`.** Check that all three repetitions pass and create separate folders under `target/artifacts/`.
5. **Adapt `StableCancelOrderTest`** following `StableCreateOrderTest`:
   - Replace `@Test` with `@RepeatedTest(3)`.
   - Use `RepetitionInfo` and `TestConfig.loadForRun(...)` in `@BeforeEach`.
   - Use `Artifacts` to keep each run's results separate; capture failures before closing the browser.
   - Keep the existing `OrderWorkflow` and cancellation assertions.
6. **Run `StableCancelOrderTest`.** Verify that all three repetitions create, cancel, and check their own orders.

No submission or commit is required for this exercise. Keep your local `config/test.properties` out of Git ;)
