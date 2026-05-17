package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.DashboardPage;
import com.cts.mfrp.petz.pages.LoginPage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.DASHBOARD_URL;
import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;
import static com.cts.mfrp.petz.constants.AppConstants.PET_OWNER_NAME;

/**
 * Pet Owner Dashboard scenario — TC021 to TC025 in one class.
 * Each @Test method is one test case; BaseTest provides a fresh WebDriver per method.
 * Reporting is auto-handled by TestListener — no need to call ExtentReportManager here.
 */
public class PetOwnerDashboardTest extends BaseTest {

    @Test(priority = 21, description =
            "PETZ_TC021 - Validate greeting block and date on /dashboard")
    public void TC021_verifyDashboardGreetingAndDate() {
        new LoginPage(driver).loginAsPetOwner();
        Assert.assertTrue(driver.getCurrentUrl().contains("/dashboard"),
                "Expected to land on /dashboard after login, but was: "
                        + driver.getCurrentUrl());

        DashboardPage dashboard = new DashboardPage(driver);

        Assert.assertTrue(dashboard.isGreetingVisible(),
                "Greeting heading 'Good <Morning|Afternoon|Evening>, <name>!' is not visible.");

        String firstName = PET_OWNER_NAME.split("\\s+")[0];
        Assert.assertTrue(dashboard.greetingContainsFirstName(firstName),
                "Greeting does not contain the user's first name: " + firstName);

        Assert.assertTrue(dashboard.isSubheadingVisible(),
                "Subheading 'Here's today's overview of the platform.' is not visible.");

        Assert.assertTrue(dashboard.isTodayDateVisible(),
                "Today's date (weekday, month, day, year) is not visible on the dashboard.");

        driver.get(DASHBOARD_URL);
    }

    @Test(priority = 22, description =
            "PETZ_TC022 - Validate red 'Animal in Distress?' banner on /dashboard")
    public void TC022_verifyEmergencyBanner() {
        new LoginPage(driver).loginAsPetOwner();
        DashboardPage dashboard = new DashboardPage(driver);

        Assert.assertTrue(dashboard.isEmergencyBannerVisible(),
                "Emergency banner 'Animal in Distress?' is not visible.");

        Assert.assertTrue(dashboard.isEmergencyCopyVisible(),
                "Banner copy ('Report it immediately - every second counts') is not visible.");

        Assert.assertTrue(dashboard.isReportNowButtonVisible(),
                "'Report Now ->' button is not visible on the banner.");

        dashboard.clickReportNow();

        new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT))
                .until(ExpectedConditions.urlContains("/rescue"));

        Assert.assertTrue(driver.getCurrentUrl().contains("/rescue/report")
                        || driver.getCurrentUrl().contains("/rescue"),
                "After clicking 'Report Now ->' the URL is not /rescue/report. Actual: "
                        + driver.getCurrentUrl());

        Assert.assertTrue(driver.getPageSource().contains("Report Animal in Need")
                        || driver.getPageSource().contains("Report Animal"),
                "'Report Animal in Need' form is not shown after clicking 'Report Now ->'.");
    }

    @Test(priority = 23, description =
            "PETZ_TC023 - Validate 4 stat tiles on /dashboard for a Pet Owner")
    public void TC023_verifyStatTiles() {
        new LoginPage(driver).loginAsPetOwner();
        DashboardPage dashboard = new DashboardPage(driver);

        Assert.assertTrue(dashboard.areStatTilesVisible(),
                "One or more of the 4 stat tiles (MY PETS, APPOINTMENTS, RESCUE REPORTS, " +
                        "ADOPTIONS) are not visible on /dashboard.");

        String src = driver.getPageSource();

        Assert.assertTrue(src.contains("MY PETS") || src.contains("My Pets"),
                "MY PETS tile is missing.");
        Assert.assertTrue(src.contains("APPOINTMENTS") || src.contains("Appointments"),
                "APPOINTMENTS tile is missing.");
        Assert.assertTrue(src.contains("RESCUE REPORTS") || src.contains("Rescue Reports"),
                "RESCUE REPORTS tile is missing.");
        Assert.assertTrue(src.contains("ADOPTIONS") || src.contains("Adoptions"),
                "ADOPTIONS tile is missing.");

        Assert.assertTrue(dashboard.areStatTileBreakdownsVisible(),
                "Stat tiles do not show their breakdown chips " +
                        "(e.g. Dogs/Cats, Upcoming/Done, Pending/Resolved, In Review/Approved, " +
                        "or the corresponding empty-state copy).");
    }

    @Test(priority = 24, description =
            "PETZ_TC024 - Validate Quick Actions cards route to the right pages")
    public void TC024_verifyQuickActions() {
        new LoginPage(driver).loginAsPetOwner();
        DashboardPage dashboard = new DashboardPage(driver);

        Assert.assertTrue(dashboard.areQuickActionsVisible(),
                "One or more Quick Action cards (Report Rescue, Browse Adoptions, " +
                        "Book Appointment, My Adoptions, My Appointments) are not visible.");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));

        String[][] actions = {
                {"Report Rescue",    "/rescue"},
                {"Browse Adoptions", "/adoption/animals"},
                {"Book Appointment", "/appointments/book"},
                {"My Adoptions",     "/adoption/my"},
                {"My Appointments",  "/appointments"}
        };

        for (String[] action : actions) {
            String label   = action[0];
            String urlPart = action[1];

            driver.get(DASHBOARD_URL);
            wait.until(ExpectedConditions.urlContains("/dashboard"));

            dashboard.clickQuickAction(label);
            wait.until(ExpectedConditions.urlContains(urlPart));

            Assert.assertTrue(driver.getCurrentUrl().contains(urlPart),
                    "Quick Action '" + label + "' did not route to " + urlPart +
                            ". Actual URL: " + driver.getCurrentUrl());
        }
    }

    @Test(priority = 25, description =
            "PETZ_TC025 - Validate sidebar items for a Pet Owner on /dashboard")
    public void TC025_verifySidebar() {
        new LoginPage(driver).loginAsPetOwner();
        DashboardPage dashboard = new DashboardPage(driver);

        Assert.assertTrue(dashboard.isSidebarVisible(),
                "Sidebar is not visible on /dashboard.");

        Assert.assertTrue(
                dashboard.sidebarContains(
                        "Dashboard", "My Adoptions", "Browse Animals", "Appointments", "Rescue"),
                "Sidebar is missing one or more expected items: " +
                        "Dashboard, My Adoptions, Browse Animals, Appointments, Rescue.");

        Assert.assertTrue(dashboard.sidebarUserLabelVisible(),
                "User widget at the bottom of the sidebar does not show the 'USER' label.");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));

        String[][] navItems = {
                {"My Adoptions",   "/adoption/my"},
                {"Browse Animals", "/adoption/animals"},
                {"Appointments",   "/appointments"},
                {"Rescue",         "/rescue"},
                {"Dashboard",      "/dashboard"}
        };

        for (String[] item : navItems) {
            String label   = item[0];
            String urlPart = item[1];

            dashboard.clickSidebarItem(label);
            wait.until(ExpectedConditions.urlContains(urlPart));

            Assert.assertTrue(driver.getCurrentUrl().contains(urlPart),
                    "Sidebar item '" + label + "' did not route to " + urlPart +
                            ". Actual URL: " + driver.getCurrentUrl());
        }
    }
}
