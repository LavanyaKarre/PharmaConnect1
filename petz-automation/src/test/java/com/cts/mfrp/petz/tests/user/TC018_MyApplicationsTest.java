package com.cts.mfrp.petz.tests.user;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.MyApplicationsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC018 - My Applications list with status badges or empty state.
 */
public class TC018_MyApplicationsTest extends BaseTest {

    @Test(groups = {"user", "regression", "positive"},
          description = "TC018 - My Applications renders list with status or empty state")
    public void TC018_MyApplications() {
        new LoginPage().open().loginAs(UserRole.PET_OWNER);
        MyApplicationsPage apps = new MyApplicationsPage().open();

        Assert.assertTrue(apps.isTitleVisible(), "Applications title not visible");
        boolean ok = apps.getCardCount() > 0
                  || apps.isEmptyStateVisible()
                  || apps.isBrowseCtaVisible();
        Assert.assertTrue(ok, "Neither applications nor empty state visible");
    }
}
