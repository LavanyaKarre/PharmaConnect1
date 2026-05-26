package com.cts.mfrp.petz.tests.authentication;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.base.DriverFactory;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.pages.RegisterPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC008 - Register rejects an already-registered email.
 * Uses the seed Pet Owner email (which exists on the live backend).
 */
public class TC008_RegisterDuplicateEmailTest extends BaseTest {

    @Test(groups = {"auth", "regression", "negative"},
          description = "TC008 - Register rejects already-used email")
    public void TC008_RegisterDuplicateEmail() {
        RegisterPage register = new RegisterPage().open();
        register.fillFullName("Test User");
        register.fillPhone("9876543210");
        register.fillEmail(AppConstants.PET_OWNER_EMAIL); // already in DB
        register.fillPassword("Admin@123");
        register.fillConfirm("Admin@123");
        register.selectAccountType("Pet Owner");
        register.clickCreate();

        try { Thread.sleep(3000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

        String url = DriverFactory.getDriver().getCurrentUrl();
        Assert.assertTrue(url.contains("/auth/register") || register.isAnyErrorVisible(),
                "Expected registration to be rejected but URL=" + url);
    }
}
