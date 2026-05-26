package com.cts.mfrp.petz.tests.hospital;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.HospitalDashboardPage;
import com.cts.mfrp.petz.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC025 - Hospital Dashboard renders stat tiles and nav cards.
 */
public class TC025_HospitalDashboardTest extends BaseTest {

    @Test(groups = {"hospital", "smoke", "regression", "positive"},
          description = "TC025 - Hospital Dashboard renders title + stat tiles + info/nav cards")
    public void TC025_HospitalDashboard() {
        new LoginPage().open().loginAs(UserRole.HOSPITAL);
        HospitalDashboardPage dashboard = new HospitalDashboardPage();

        Assert.assertTrue(dashboard.isTitleVisible(), "Hospital dashboard title not visible");
        Assert.assertTrue(dashboard.getStatTileCount() >= 1,
                "Expected >=1 stat tile but got " + dashboard.getStatTileCount());
    }
}
