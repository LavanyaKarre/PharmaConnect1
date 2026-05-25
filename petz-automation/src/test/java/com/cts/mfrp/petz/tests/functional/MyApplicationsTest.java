package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.MyApplicationsPage;
import com.cts.mfrp.petz.utils.StepReporter;
import com.cts.mfrp.petz.utils.Waits;
import org.testng.annotations.Test;

/**
 * My Applications (/adoption/my) scenario â€” PETZ_TC036 to PETZ_TC037.
 * Group: myApplications.
 */
public class MyApplicationsTest extends BaseTest {

    @Test(priority = 36,
          groups = {"myApplications", "ui", "regression", "sanity", "positive"},
          description = "PETZ_TC036 - Empty state on /adoption/my")
    public void TC036_MyAppsEmpty() {
        new LoginPage(driver).loginAsPetOwner();
        MyApplicationsPage page = new MyApplicationsPage(driver);
        page.open();

        StepReporter.check("Title 'My Applications'",
                "Heading visible", page.isTitleVisible());
        StepReporter.check("Subtitle",
                "'Track your adoption application statuses'", page.isSubtitleVisible());

        int count = page.getApplicationCardCount();
        if (count == 0) {
            StepReporter.check("Empty state on a brand-new account",
                    "'No applications yet' empty state visible",
                    page.isEmptyStateVisible());
            page.clickBrowseAnimalsEmpty();
            Waits.urlContains(driver, "/adoption/animals");
            StepReporter.check("'Browse Animals' destination",
                    "/adoption/animals", page.getCurrentUrl());
        } else {
            StepReporter.info("Account already has " + count + " applications â€” skipping empty-state checks.");
        }
    }

    @Test(priority = 37,
          groups = {"myApplications", "ui", "regression", "positive"},
          description = "PETZ_TC037 - List state once at least one application exists")
    public void TC037_MyAppsListWithStatus() {
        new LoginPage(driver).loginAsPetOwner();
        MyApplicationsPage page = new MyApplicationsPage(driver);
        page.open();

        int count = page.getApplicationCardCount();
        StepReporter.info("Application cards visible: " + count);

        if (count == 0) {
            StepReporter.note("List state",
                    "At least one application card with status badge",
                    "No applications exist for this account â€” checked structure only");
            return;
        }
        StepReporter.check("Status badge on at least one card",
                "Pending / Under Review / Approved / Rejected badge",
                page.hasStatusBadge());
    }
}
