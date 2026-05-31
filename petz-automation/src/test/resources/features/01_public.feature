Feature: Public visitor journey
  Anonymous visitors can view the landing page and register a new account.

  Scenario: TC01 - Landing page renders all key sections
    Given I open the PETZ landing page
    Then the logo, hero, stats, features and footer are all visible

  Scenario: TC02 - A new user can register
    Given I open the registration page
    When I register a new account with:
      | fullName  | phone      | password  | accountType |
      | Test User | 9876543210 | Admin@123 | Pet Owner   |
    Then I should land on the login or dashboard page
