package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.RescueReportsListPage;
import com.cts.mfrp.petz.utils.StepReporter;
import org.testng.annotations.Test;

/**
 * Rescue Reports list (/rescue) scenario — PETZ_TC047 to PETZ_TC048.
 * Group: rescueList.
 */
public class RescueListTest extends BaseTest {

    @Test(priority = 47, groups = {"rescueList"},
          description = "PETZ_TC047 - Layout of /rescue")
    public void TC047_RescueListLayout() {
        new LoginPage(driver).loginAsPetOwner();
        RescueReportsListPage page = new RescueReportsListPage(driver);
        page.open();

        StepReporter.check("Title 'Rescue Reports'",
                "Heading visible", page.isTitleVisible());
        StepReporter.check("Subtitle copy",
                "'... reported that need help'", page.isSubtitleVisible());
        StepReporter.check("'Report Animal' CTA (top-right)",
                "Red Report Animal button visible", page.isReportAnimalBtnVisible());

        page.waitForLoadingToFinish();
        int count = page.getReportCardCount();
        StepReporter.note("After spinner finishes",
                "Either empty state or list of report cards",
                count == 0 ? "empty state" : count + " report card(s)");
    }

    @Test(priority = 48, groups = {"rescueList"},
          description = "PETZ_TC048 - 'Report Animal' CTA navigates to /rescue/report")
    public void TC048_RescueListReportButton() {
        new LoginPage(driver).loginAsPetOwner();
        RescueReportsListPage page = new RescueReportsListPage(driver);
        page.open();
        page.clickReportAnimal();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        StepReporter.check("After clicking 'Report Animal'",
                "/rescue/report", page.getCurrentUrl());
        StepReporter.check("Form heading present",
                "'Report Animal in Need' on page",
                driver.getPageSource().contains("Report Animal"));
    }
}
