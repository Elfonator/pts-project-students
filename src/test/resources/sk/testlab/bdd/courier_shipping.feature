@example
Feature: Courier delivery pricing
  The free-shipping threshold is evaluated after the coupon discount.

  Scenario: Free-shipping threshold is applied after discount
    Given the user has "BAG-001" in quantity 4
    And the user applies coupon "STUDENT10"
    When the user chooses courier delivery
    Then shipping costs 490 cents
    And the total amount is 9490 cents
