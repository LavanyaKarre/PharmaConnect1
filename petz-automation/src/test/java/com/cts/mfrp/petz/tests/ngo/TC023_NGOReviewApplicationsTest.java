package com.cts.mfrp.petz.tests.ngo;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.AdoptionApplicationsPage;
import com.cts.mfrp.petz.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC023 - NGO Review Applications - list view + filters render.
 */
public class TC023_NGOReviewApplicationsTest extends BaseTest {

    @Test(groups = {"ngo", "regression", "positive"},
          description = "TC023 - NGO Review Applications page renders with cards or empty state")
    public void TC023_NGOReviewApplications() {
        new LoginPage().open().loginAs(UserRole.NGO);
        AdoptionApplicationsPage apps = new AdoptionApplicationsPage().open();

        Assert.assertTrue(apps.isTitleVisible(), "Applications page title not visible");
        boolean ok = apps.getCardCount() > 0
                  || apps.isEmptyStateVisible()
                  || apps.isSearchVisible();
        Assert.assertTrue(ok, "Applications page rendered without cards / empty state / search");
    }
}
