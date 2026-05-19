package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.NGODashboardPage;
import com.cts.mfrp.petz.utils.StepReporter;
import com.cts.mfrp.petz.utils.Waits;
import org.testng.annotations.Test;

/**
 * NGO Dashboard (/ngo) scenario â€” PETZ_TC059 to PETZ_TC062.
 * Group: ngoDashboard.
 */
public class NGODashboardTest extends BaseTest {

    @Test(priority = 59,
          groups = {"ngoDashboard", "functional", "regression", "sanity", "positive"},
          description = "PETZ_TC059 - NGO dashboard layout")
    public void TC059_NGODashLayout() {
        new LoginPage(driver).loginAsNgo();
        NGODashboardPage page = new NGODashboardPage(driver);
        page.open();

        StepReporter.check("Title 'NGO Dashboard'",
                "Heading visible", page.isTitleVisible());
        StepReporter.check("Subtitle",
                "'Manage your rescue operations and adoption listings'",
                page.isSubtitleVisible());
        StepReporter.check("Four stat tiles",
                "ANIMALS LISTED / TOTAL RESCUES / APPLICATIONS / COMPLETED RESCUES",
                page.areStatTilesVisible());
    }

    @Test(priority = 60,
          groups = {"ngoDashboard", "functional", "regression", "positive"},
          description = "PETZ_TC060 - Two donut charts (Rescue Pipeline / Application Status)")
    public void TC060_NGODashCharts() {
        new LoginPage(driver).loginAsNgo();
        NGODashboardPage page = new NGODashboardPage(driver);
        page.open();

        StepReporter.check("Rescue Pipeline panel",
                "Assigned / In Progress / Completed legend", page.isRescuePipelineChartVisible());
        StepReporter.check("Application Status panel",
                "Pending / Approved / Rejected legend", page.isApplicationStatusChartVisible());
        StepReporter.check("'See Queue' link",
                "Link visible", page.isSeeQueueLinkVisible());
        StepReporter.check("'Review' link",
                "Link visible", page.isReviewLinkVisible());
    }

    @Test(priority = 61,
          groups = {"ngoDashboard", "functional", "regression", "positive"},
          description = "PETZ_TC061 - Three Quick Action cards route correctly")
    public void TC061_NGODashQuickActions() {
        LoginPage login = new LoginPage(driver);
        login.loginAsNgo();
        NGODashboardPage page = new NGODashboardPage(driver);
        page.open();

        StepReporter.check("Quick Actions row visible",
                "Manage Animals / Rescue Queue / Adoption Applications",
                page.areQuickActionsVisible());

        page.clickManageAnimals();
        Waits.urlContains(driver, "/ngo/animals");
        StepReporter.check("Manage Animals destination",
                "/ngo/animals", page.getCurrentUrl());

        page.open();
        page.clickRescueQueue();
        Waits.urlContains(driver, "/ngo/rescues");
        StepReporter.check("Rescue Queue destination",
                "/ngo/rescues", page.getCurrentUrl());

        page.open();
        page.clickAdoptionApplications();
        Waits.urlContains(driver, "/ngo/applications");
        StepReporter.check("Adoption Applications destination",
                "/ngo/applications", page.getCurrentUrl());
    }

    @Test(priority = 62,
          groups = {"ngoDashboard", "functional", "regression", "sanity", "positive"},
          description = "PETZ_TC062 - NGO sidebar items")
    public void TC062_NGODashSidebar() {
        new LoginPage(driver).loginAsNgo();
        NGODashboardPage page = new NGODashboardPage(driver);
        page.open();

        String src = driver.getPageSource();
        for (String item : new String[]{"NGO Dashboard", "Rescue Queue", "My Animals", "Applications"}) {
            StepReporter.check("Sidebar item '" + item + "'",
                    item + " present in sidebar", src.contains(item));
        }
        StepReporter.check("User widget shows 'NGO' label",
                "'NGO' label", src.toUpperCase().contains("NGO"));
    }
}
