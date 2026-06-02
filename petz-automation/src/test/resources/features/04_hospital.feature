Feature: Hospital journey
  A hospital admin reviews the dashboard, adds a doctor and confirms an appointment.
  Logs in once for the whole journey.

  Background:
    Given I am logged in as a hospital

  Scenario: TC13 - Hospital lands on its dashboard
    Then I should be on the "/hospital" page
    And I see the hospital dashboard

  Scenario: TC14 - Hospital adds a doctor
    When I add a doctor with the details:
      | fullName         | specialization | slotDuration |
      | Dr. Anita Sharma | General        | 30           |

  Scenario: TC15 - Hospital confirms the first pending appointment
    Then the appointments queue renders and I confirm the first one if present
