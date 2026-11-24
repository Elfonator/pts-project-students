package sk.testlab.mobile.screens;

import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import sk.testlab.mobile.support.MobileUi;

/**
 * HOMEWORK / class TODO — native catalog screen.
 * Use {@link MobileUi} helpers ({@code click}, {@code type}, {@code visible}, …).
 * SKU in ids is lower-case with underscore, KB-001 → {@code kb_001}.
 * See the EXAMPLE - MobileLoginCatalogExampleTest
 */
public final class CatalogScreen {
    private final MobileUi ui;

    public CatalogScreen(AndroidDriver driver, WebDriverWait wait) {
        this.ui = new MobileUi(driver, wait);
    }

    public CatalogScreen(MobileUi ui) {
        this.ui = ui;
    }

    // Open the Produkty tab ({@code nav_catalog})
    public void openCatalogTab() {
        // TODO 1: ui.click("nav_catalog");
        throw new UnsupportedOperationException("TODO: openCatalogTab");
    }

    // Prefer {@code loading_overlay} gone and/or {@code catalog_ready} visible
    public void waitUntilReady() {
        // TODO 2: ui.waitUntilGone("loading_overlay"); ui.visible("catalog_ready");
        throw new UnsupportedOperationException("TODO: waitUntilReady");
    }

    // Type into {@code search_input} and tap {@code search_submit}
    public void search(String query) {
        // TODO 3: ui.type(...); ui.click("search_submit"); waitUntilReady();
        throw new UnsupportedOperationException("TODO: search");
    }

    // Return visible text of {@code product_price_<sku>} for a catalog SKU
    // {@code KB-001} → id {@code product_price_kb_001}
    public String productPrice(String sku) {
        // TODO 4: return ui.visible("product_price_" + skuToIdSuffix(sku)).getText();
        throw new UnsupportedOperationException("TODO: productPrice");
    }

    // Helper: KB-001 → kb_001
    public static String skuToIdSuffix(String sku) {
        return sku.trim().toLowerCase().replace('-', '_');
    }
}
