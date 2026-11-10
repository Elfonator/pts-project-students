package sk.testlab.bdd.steps;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import sk.testlab.bdd.support.ScenarioContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * TASK — implement only the steps unique to create-order.
 * Coupon and courier steps are reused from {@link CourierShippingSteps}
 * (same Gherkin phrase = one-step definition globally).
 * Prefer {@link sk.testlab.workflows.OrderWorkflow} and page objects from
 * {@link sk.testlab.bdd.support.ScenarioContext}. Keep selectors out of this class.
 */
public class CreateOrderStepsTask {

    @Given("a logged-in user")
    public void aLoggedInUser() {
        ScenarioContext ctx = ScenarioContext.get();
        ctx.login.open();
        ctx.login.login(ctx.config.email, ctx.config.password);
        ctx.catalog.waitUntilReady();
    }

    @When("the user adds the reference cart items")
    public void theUserAddsTheReferenceCartItems() {
        ScenarioContext ctx = ScenarioContext.get();
        // Same cart as OrderWorkflow / ApiQuoteTest: KB-001 × 2, MS-001 × 1
        ctx.catalog.addProduct("KB-001", 1);
        ctx.catalog.addProduct("KB-001", 2);
        ctx.catalog.addProduct("MS-001", 3);
        ctx.cart.open();
    }

    @When("the user places the order")
    public void theUserPlacesTheOrder() {
        ScenarioContext ctx = ScenarioContext.get();
        ctx.cart.proceedToCheckout();
        ctx.checkout.waitUntilReady();
        ctx.checkout.fillCustomer(
                "Student Testovaci",
                "student@example.test",
                "Testovacia 12",
                "Nitra",
                "949 01");
        ctx.checkout.acceptTerms();
        ctx.checkout.placeOrder();
        ctx.order.getOrderId();
    }

    @Then("the order status is {string}")
    public void theOrderStatusIs(String expectedStatus) {
        assertEquals(expectedStatus, ScenarioContext.get().order.getStatus());
    }

    @Then("the order total is {int} cents")
    public void theOrderTotalIsCents(int expectedTotal) {
        int actual = Integer.parseInt(ScenarioContext.get().order.getTotalCents());
        assertEquals(expectedTotal, actual);
    }
}
