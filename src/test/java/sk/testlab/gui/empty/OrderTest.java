package sk.testlab.gui.empty;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.support.TestConfig;

import java.io.IOException;

class OrderTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private TestConfig config;

    @BeforeEach
    void setUp() {

        // TODO:
        // 1. nacitaj konfiguraciu
        // 2. vytvor WebDriver
        // 3. vytvor WebDriverWait

    }

    @AfterEach
    void tearDown() {

        // TODO:
        // zatvor browser

    }

    @Test
    void createOrder() throws IOException {

        try {

            // =========================
            // ARRANGE
            // =========================

            // TODO:
            // otvor aplikaciu

            // TODO:
            // prihlas pouzivatela


            // =========================
            // ACT
            // =========================

            // TODO:
            // pridaj KB-001 2x

            // TODO:
            // pridaj MS-001 1x

            // TODO:
            // otvor kosik

            // TODO:
            // nastav dopravu courier

            // TODO:
            // aplikuj kupon STUDENT10

            // TODO:
            // prejdi na checkout

            // TODO:
            // vypln zakaznicke udaje

            // TODO:
            // vytvor objednavku


            // =========================
            // UUID
            // =========================

            // TODO:
            // najdi UUID vytvorenej objednavky

            // TODO:
            // uloz UUID do suboru


            // =========================
            // ASSERT
            // =========================

            // TODO:
            // najdi vyslednu cenu

            // TODO:
            // zisti cenu v centoch

            // TODO:
            // zisti viditelnu cenu

            // TODO:
            // zisti stav objednavky

            // TODO:
            // over cenu 8563 centov

            // TODO:
            // over viditelnu cenu 85,63 €

            // TODO:
            // over stav created

        } catch (RuntimeException | AssertionError error) {

            // TODO:
            // pri zlyhani uloz screenshot

            // TODO:
            // povodnu chybu posli dalej
        }
    }

    // TODO:
    // vytvor locator pomocou data-testid
    private By testId(String id) {
        return null;
    }

    // TODO:
    // pockaj, kym je element viditelny
    private WebElement visible(String id) {
        return null;
    }

    // TODO:
    // pockaj, kym je element klikatelny, a klikni
    private void click(String id) {

    }

    // TODO:
    // najdi input, vymaz ho a napis hodnotu
    private void type(String id, String value) {

    }

    // TODO:
    // uloz UUID do target/artifacts/order-id.txt
    private void saveOrderId(String orderId) throws IOException {

    }

    // TODO:
    // uloz screenshot do target/artifacts
    private void takeScreenshot(String fileName) {

    }

    // TODO:
    // zjednot medzery vo viditelnej cene
    private String normalizeSpaces(String text) {
        return text;
    }
}