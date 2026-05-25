    package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.NotificationsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Notifications scenario â€” PETZ_TS_12 (TC057 & TC058).
 * Both tests belong to the "notifications" TestNG group.
 */
public class NotificationsTest extends BaseTest {

    @Test(priority = 57,
          groups = {"notifications", "ui", "regression", "sanity", "positive"},
          description = "PETZ_TC057 - Bell icon opens /notifications with title and content")
    public void TC057_verifyNotificationsRender() {
        new LoginPage(driver).loginAsPetOwner();

        NotificationsPage page = new NotificationsPage(driver);
        page.clickBell();

        Assert.assertTrue(page.getCurrentUrl().contains("/notifications"),
                "Bell icon did not navigate to /notifications.");
        Assert.assertTrue(page.isTitleVisible(), "Notifications title not visible.");
        Assert.assertTrue(page.isSubtitleVisible(),
                "Subtitle 'Stay updated on your activity' not visible.");
        Assert.assertTrue(page.isEmptyOrListVisible(),
                "Neither empty state nor a list of notifications was shown.");
    }

    @Test(priority = 58,
          groups = {"notifications", "functional", "regression", "positive"},
          description = "PETZ_TC058 - Back arrow on /notifications returns to previous page")
    public void TC058_verifyNotificationsBackArrow() {
        new LoginPage(driver).loginAsPetOwner();

        NotificationsPage page = new NotificationsPage(driver);
        page.clickBell();
        page.clickBack();

        Assert.assertTrue(page.getCurrentUrl().contains("/dashboard"),
                "Back arrow did not return to /dashboard. Actual: " + page.getCurrentUrl());
    }
}
