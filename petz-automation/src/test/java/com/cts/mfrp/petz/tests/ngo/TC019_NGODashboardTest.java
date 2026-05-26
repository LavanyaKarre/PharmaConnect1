package com.cts.mfrp.petz.tests.ngo;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.NGODashboardPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC019 - NGO Dashboard renders stat tiles and quick actions.
 */
public class TC019_NGODashboardTest extends BaseTest {

    @Test(groups = {"ngo", "smoke", "regression", "positive"},
          description = "TC019 - NGO Dashboard renders title + stat tiles + quick actions")
    public void TC019_NGODashboard() {
        new LoginPage().open().loginAs(UserRole.NGO);
        NGODashboardPage dashboard = new NGODashboardPage();

        Assert.assertTrue(dashboard.isTitleVisible(), "NGO dashboard title not visible");
        Assert.assertTrue(dashboard.getStatTileCount() >= 1,
                "Expected >=1 NGO stat label in page source, got " + dashboard.getStatTileCount());
        Assert.assertTrue(dashboard.hasQuickActions(), "NGO quick actions not visible");
    }
}
