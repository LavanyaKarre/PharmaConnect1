Feature: Pet Owner journey
  A pet owner manages pets, books a vet appointment, reports a rescue and adopts.
  Logs in once for the whole journey (Background reuses the shared session).

  Background:
    Given I am logged in as a pet owner

  Scenario: TC03 - Pet Owner lands on the dashboard
    Then I should be on the "/dashboard" page
    And the greeting, emergency banner and at least one KPI tile are visible

  Scenario: TC04 - Pet Owner adds a pet
    When I add a pet with the details:
      | name  | age | description       |
      | Buddy | 3   | Friendly Labrador |

  Scenario: TC05 - Pet Owner books a vet appointment
    When I book a vet appointment with:
      | dateOffsetDays | reason                      |
      | 7              | Annual vaccination check-up |

  Scenario: TC06 - Pet Owner reports a rescue
    When I report a rescue with:
      | animalType | urgency | area       | landmark                          | condition                                | reporterPhone |
      | Dog        | Medium  | Anna Nagar | Near petrol bunk, behind bus stop | Limping with visible wound on the left leg | 9876543210    |

  Scenario: TC07 - Pet Owner browses adoption listings
    Then I can see the adoption listings

  Scenario: TC08 - Pet Owner applies to adopt the first animal
    When I apply to adopt the first animal with:
      | reason                                                     |
      | I have a fenced yard and prior experience with rescues.    |

  Scenario: TC09 - Pet Owner sees their appointments and applications
    Then my appointments and applications pages both render
