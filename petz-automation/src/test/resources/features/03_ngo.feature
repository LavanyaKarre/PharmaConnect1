Feature: NGO journey
  An NGO reviews its dashboard, posts an adoptable animal and reviews applications.
  Logs in once for the whole journey.

  Background:
    Given I am logged in as an NGO

  Scenario: TC10 - NGO lands on its dashboard
    Then I should be on the "/ngo" page
    And I see the NGO dashboard tiles and quick actions

  Scenario: TC11 - NGO adds an adoptable animal
    When I add an animal with the details:
      | name | species | age | description          |
      | Rex  | Dog     | 4   | Healthy and friendly |

  Scenario: TC12 - NGO reviews adoption applications
    Then the adoption applications screen renders
