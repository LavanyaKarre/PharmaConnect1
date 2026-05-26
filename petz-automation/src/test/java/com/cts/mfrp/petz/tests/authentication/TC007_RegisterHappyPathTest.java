package com.cts.mfrp.petz.tests.authentication;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.base.DriverFactory;
import com.cts.mfrp.petz.pages.RegisterPage;
import com.cts.mfrp.petz.utils.RandomDataGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC007 - Register happy path for a new Pet Owner.
 * Fresh email each run via RandomDataGenerator so we never hit duplicate-email error.
 */
public class TC007_RegisterHappyPathTest extends BaseTest {

    @Test(groups = {"auth", "regression", "positive"},
          description = "TC007 - New Pet Owner can register successfully")
    public void TC007_RegisterHappyPath() {
        RegisterPage register = new RegisterPage().open();
        register.fillFullName("Test User");
        register.fillPhone("9876543210");
        register.fillEmail(RandomDataGenerator.randomEmail());
        register.fillPassword("Admin@123");
        register.fillConfirm("Admin@123");
        register.selectAccountType("Pet Owner");
        register.clickCreate();

        try { Thread.sleep(3000); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }

        String url = DriverFactory.getDriver().getCurrentUrl();
        Assert.assertTrue(url.contains("/auth/login") || url.contains("/dashboard"),
                "After register, expected /auth/login or /dashboard but got " + url);
    }
}
