package com.cts.mfrp.petz.tests.user;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.UserDashboardPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC009 - User Dashboard renders KPIs and greeting.
 */
public class TC009_UserDashboardTest extends BaseTest {

    @Test(groups = {"user", "smoke", "regression", "positive"},
          description = "TC009 - Pet Owner Dashboard renders greeting + KPI tiles + emergency banner")
    public void TC009_UserDashboard() {
        new LoginPage().open().loginAs(UserRole.PET_OWNER);
        UserDashboardPage dashboard = new UserDashboardPage();

        Assert.assertTrue(dashboard.isGreetingVisible(),         "Greeting not visible");
        Assert.assertTrue(dashboard.isEmergencyBannerVisible(),  "Emergency banner not visible");
        Assert.assertTrue(dashboard.getKpiTileCount() >= 1,
                "Expected >=1 KPI tile but got " + dashboard.getKpiTileCount());
    }
}
