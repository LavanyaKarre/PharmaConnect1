package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LandingPage;
import com.cts.mfrp.petz.pages.RegisterPage;
import com.cts.mfrp.petz.utils.ExcelDataProvider;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.Map;

/**
 * Public (anonymous) journey: landing render + new-user registration.
 * No login at @BeforeClass.
 *
 * Story:
 *   TC01 - We open the public landing page and confirm the marketing
 *          sections render.
 *   TC02 - A brand-new user registers using values from
 *          register-data.xlsx (a fresh email is generated inline so we
 *          never collide with an existing account).
 */
public class PublicJourneyTest extends BaseTest {

    @Override
    protected UserRole role() {
        return null; // anonymous - skip @BeforeClass login
    }

    @Test(priority = 1, description = "TC01 - Landing page renders all key sections")
    public void TC01_LandingRenders() {
        LandingPage landing = new LandingPage().open();
        Assert.assertTrue(landing.isLogoVisible(),     "Landing logo not visible");
        Assert.assertTrue(landing.isHeroVisible(),     "Hero section not visible");
        Assert.assertTrue(landing.isStatsVisible(),    "Stats strip not visible");
        Assert.assertTrue(landing.isFeaturesVisible(), "Feature cards not visible");
        Assert.assertTrue(landing.isFooterVisible(),   "Footer not visible");
    }

    @Test(priority = 2, dataProvider = "registerRow",
          description = "TC02 - New user can register with a fresh email")
    public void TC02_Register(Map<String, String> row) {
        String email = "u" + System.currentTimeMillis() + "@petz.com";

        new RegisterPage().open()
                .fillFullName(row.get("fullName"))
                .fillPhone(row.get("phone"))
                .fillEmail(email)
                .fillPassword(row.get("password"))
                .fillConfirm(row.get("password"))
                .selectAccountType(row.get("accountType"))
                .clickCreate();

        // Registration is async ("Creating account...") - wait for the route to change.
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15))
                    .until(d -> !d.getCurrentUrl().contains("/auth/register"));
        } catch (Exception ignored) { /* assertion handles message */ }

        String url = driver.getCurrentUrl();
        Assert.assertTrue(url.contains("/auth/login") || url.contains("/dashboard"),
                "After register expected /auth/login or /dashboard, got " + url);
    }

    @DataProvider(name = "registerRow")
    public Object[][] registerRow() {
        Map<String, String> row = ExcelDataProvider
                .readSheet(AppConstants.REGISTER_DATA_XLSX, "happy_path").get(0);
        return new Object[][] { { row } };
    }
}
