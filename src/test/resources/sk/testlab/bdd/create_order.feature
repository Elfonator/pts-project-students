@task
Feature: Create reference order
  Rewrite the known GUI create-order flow as an executable specification.
  Reuse OrderWorkflow or existing page objects in the step definitions - not raw Selenium.
  Coupon and courier lines reuse the EXAMPLE step definitions (one phrase = one Java method).

  Scenario: User places the reference order
    Given a logged-in user
    When the user adds the reference cart items
    And the user applies coupon "STUDENT10"
    And the user chooses courier delivery
    And the user places the order
    Then the order status is "created"
    And the order total is 8563 cents
