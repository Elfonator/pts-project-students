@example @outline
Feature: Courier delivery pricing with examples
  One rule, many data rows — free-shipping threshold after discount.

  Scenario Outline: Free-shipping threshold is applied after discount
    Given the user has "BAG-001" in quantity 4
    And the user applies coupon "<coupon>"
    When the user chooses "<delivery>" delivery
    Then shipping costs <shipping> cents
    And the total amount is <total> cents

    Examples:
      | coupon    | delivery | shipping | total |
      | STUDENT10 | courier  | 490      | 9490  |
      | STUDENT10 | pickup   | 0        | 9000  |