package com.cts.mfrp.petz.tests.user;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.RescueReportsListPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC015 - My Rescue Reports list with urgency badges.
 */
public class TC015_MyRescueReportsTest extends BaseTest {

    @Test(groups = {"user", "regression", "positive"},
          description = "TC015 - My Rescue Reports renders list with urgency badges or CTA")
    public void TC015_MyRescueReports() {
        new LoginPage().open().loginAs(UserRole.PET_OWNER);
        RescueReportsListPage rescues = new RescueReportsListPage().open();

        Assert.assertTrue(rescues.isTitleVisible(), "Rescue reports title not visible");
        boolean ok = rescues.getReportCount() > 0 || rescues.isReportButtonVisible();
        Assert.assertTrue(ok, "Neither rescue cards nor a Report CTA were visible");
    }
}
