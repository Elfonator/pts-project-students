package sk.testlab.bdd.steps;

import io.cucumber.java.PendingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

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
        // TODO: open the app and log in with ScenarioContext config credentials
        throw new PendingException("Wire login via LoginPage / ScenarioContext");
    }

    @When("the user adds the reference cart items")
    public void theUserAddsTheReferenceCartItems() {
        // TODO: KB-001 × 2 and MS-001 × 1 (same cart as OrderWorkflow / ApiQuoteTest)
        throw new PendingException("Add reference products via CatalogPage or OrderWorkflow");
    }

    @When("the user places the order")
    public void theUserPlacesTheOrder() {
        // TODO: checkout customer details, accept terms, place order
        throw new PendingException("Complete checkout via CheckoutPage or OrderWorkflow");
    }

    @Then("the order status is {string}")
    public void theOrderStatusIs(String expectedStatus) {
        // TODO: assert OrderDetailPage status
        throw new PendingException("Assert order status via OrderDetailPage");
    }

    @Then("the order total is {int} cents")
    public void theOrderTotalIsCents(int expectedTotal) {
        // TODO: assert OrderDetailPage total (data-cents)
        throw new PendingException("Assert order total via OrderDetailPage");
    }
}
