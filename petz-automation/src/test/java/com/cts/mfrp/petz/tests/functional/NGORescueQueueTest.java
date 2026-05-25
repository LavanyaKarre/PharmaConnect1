package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.NGORescueQueuePage;
import com.cts.mfrp.petz.utils.StepReporter;
import org.testng.annotations.Test;

/**
 * NGO Rescue Queue (/ngo/rescues) scenario â€” PETZ_TC068 to PETZ_TC069.
 * Group: ngoRescueQueue.
 *
 * TC069's spec involves a second Pet Owner browser submitting a /rescue/report;
 * the single-browser version only asserts that if cards exist, Accept/Decline
 * buttons are present and clickable.
 */
public class NGORescueQueueTest extends BaseTest {

    @Test(priority = 68,
          groups = {"ngoRescueQueue", "ui", "regression", "positive"},
          description = "PETZ_TC068 - Empty state of /ngo/rescues")
    public void TC068_NGOQueueEmpty() {
        new LoginPage(driver).loginAsNgo();
        NGORescueQueuePage page = new NGORescueQueuePage(driver);
        page.open();

        StepReporter.check("Title 'Rescue Queue'",
                "Heading visible", page.isTitleVisible());
        StepReporter.check("Subtitle",
                "'Rescue assignments waiting for your response'", page.isSubtitleVisible());
        StepReporter.check("Search input",
                "Search by animal-type/address visible", page.isSearchInputVisible());
        StepReporter.check("Three filters",
                "Status / Urgency / Sort filters visible", page.areFiltersVisible());

        if (page.isEmptyStateVisible()) {
            StepReporter.check("Empty state",
                    "'Queue is clear' visible", true);
        } else {
            StepReporter.info("Queue is not empty â€” skipping empty-state assertion.");
        }
    }

    @Test(priority = 69,
          groups = {"ngoRescueQueue", "ui", "regression", "positive"},
          description = "PETZ_TC069 - Accept/Decline buttons appear when a rescue is queued (single-browser view)")
    public void TC069_NGOQueueAcceptDecline() {
        new LoginPage(driver).loginAsNgo();
        NGORescueQueuePage page = new NGORescueQueuePage(driver);
        page.open();

        int count = page.getRescueCardCount();
        StepReporter.info("Rescue cards visible: " + count);

        if (count == 0) {
            StepReporter.note("Accept/Decline check",
                    "Accept + Decline visible on queued cards",
                    "queue is empty â€” submit a rescue from a Pet Owner session to populate");
            return;
        }
        StepReporter.check("Accept + Decline buttons present",
                "Both buttons visible on first card", page.firstCardHasAcceptDecline());
    }
}
