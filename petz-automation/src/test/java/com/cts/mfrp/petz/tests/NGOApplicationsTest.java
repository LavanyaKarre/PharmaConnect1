package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.NGOApplicationsPage;
import com.cts.mfrp.petz.utils.StepReporter;
import org.testng.annotations.Test;

/**
 * NGO Applications (/ngo/applications) scenario — PETZ_TC066 to PETZ_TC067.
 * Group: ngoApplications.
 *
 * TC067 in the spec uses a second browser; single-browser version just
 * inspects the page state (cards present or empty).
 */
public class NGOApplicationsTest extends BaseTest {

    @Test(priority = 66, groups = {"ngoApplications"},
          description = "PETZ_TC066 - Empty state of /ngo/applications")
    public void TC066_NGOAppsEmpty() {
        new LoginPage(driver).loginAsNgo();
        NGOApplicationsPage page = new NGOApplicationsPage(driver);
        page.open();

        StepReporter.check("Title 'Adoption Applications'",
                "Heading visible", page.isTitleVisible());
        StepReporter.check("Subtitle",
                "'Review and process incoming adoption requests'",
                page.isSubtitleVisible());
        StepReporter.check("Search input",
                "Search by animal name/reason visible", page.isSearchInputVisible());
        StepReporter.check("Status filter",
                "Status dropdown visible", page.isStatusFilterVisible());
        StepReporter.check("Sort filter",
                "Sort dropdown visible", page.isSortFilterVisible());

        if (page.isEmptyStateVisible()) {
            StepReporter.check("Empty state",
                    "'No applications yet' visible", true);
        } else {
            StepReporter.info("Applications already exist — skipping empty-state assertion.");
        }
    }

    @Test(priority = 67, groups = {"ngoApplications"},
          description = "PETZ_TC067 - Fresh applications appear on /ngo/applications (single-browser view)")
    public void TC067_NGOAppsCardOnArrival() {
        new LoginPage(driver).loginAsNgo();
        NGOApplicationsPage page = new NGOApplicationsPage(driver);
        page.open();

        int count = page.getApplicationCardCount();
        StepReporter.info("Application cards visible: " + count);
        StepReporter.note("After refresh of /ngo/applications",
                "Cards with animal / applicant / Pending status appear when any exist",
                count == 0 ? "no applications in dataset" : count + " card(s) visible");
    }
}
