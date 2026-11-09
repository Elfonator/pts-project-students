package sk.testlab.bdd.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import sk.testlab.bdd.support.ScenarioContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** EXAMPLE — presentation scenario wired to existing page objects. */
public class CourierShippingSteps {

    @Given("the user has {string} in quantity {int}")
    public void theUserHasProductInQuantity(String sku, int quantity) {
        ScenarioContext ctx = ScenarioContext.get();

        ctx.login.open();
        ctx.login.login(ctx.config.email, ctx.config.password);
        ctx.catalog.waitUntilReady();

        for (int count = 1; count <= quantity; count++) {
            ctx.catalog.addProduct(sku, count);
        }

        ctx.cart.open();
    }

    @Given("the user applies coupon {string}")
    public void theUserAppliesCoupon(String coupon) {
        ScenarioContext.get().cart.applyCoupon(coupon);
    }

    @When("the user chooses courier delivery")
    public void theUserChoosesCourierDelivery() {
        ScenarioContext.get().cart.selectDelivery("courier");
    }

    /** Used by Scenario Outline rows (quoted delivery value from Examples). */
    @When("the user chooses {string} delivery")
    public void theUserChoosesDelivery(String delivery) {
        ScenarioContext.get().cart.selectDelivery(delivery);
    }

    @Then("shipping costs {int} cents")
    public void shippingCostsCents(int expected) {
        assertEquals(expected, ScenarioContext.get().cart.shippingCents());
    }

    @Then("the total amount is {int} cents")
    public void theTotalAmountIsCents(int expected) {
        assertEquals(expected, ScenarioContext.get().cart.totalCents());
    }
}
