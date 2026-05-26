package com.cts.mfrp.petz.tests.user;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.BrowseAdoptionPage;
import com.cts.mfrp.petz.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC016 - Browse Adoption - adoptable animals listing renders.
 */
public class TC016_BrowseAdoptionTest extends BaseTest {

    @Test(groups = {"user", "regression", "positive"},
          description = "TC016 - Browse Adoption shows adoptable animal cards or no-results state")
    public void TC016_BrowseAdoption() {
        new LoginPage().open().loginAs(UserRole.PET_OWNER);
        BrowseAdoptionPage browse = new BrowseAdoptionPage().open();

        boolean ok = browse.getPetCardCount() > 0 || browse.isNoResultsVisible();
        Assert.assertTrue(ok, "Browse Adoption: expected pet cards or no-results state");
    }
}
