package com.cts.mfrp.petz.tests.user;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.MyAppointmentsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC013 - My Appointments list with status badges or empty state.
 */
public class TC013_MyAppointmentsTest extends BaseTest {

    @Test(groups = {"user", "regression", "positive"},
          description = "TC013 - My Appointments renders list with status badges or empty state")
    public void TC013_MyAppointments() {
        new LoginPage().open().loginAs(UserRole.PET_OWNER);
        MyAppointmentsPage appts = new MyAppointmentsPage().open();

        Assert.assertTrue(appts.isTitleVisible(), "My Appointments title not visible");
        boolean ok = appts.getApptCount() > 0
                  || appts.isEmptyStateVisible()
                  || appts.isBookNowVisible();
        Assert.assertTrue(ok, "Neither appointments nor an empty state were visible");
    }
}
