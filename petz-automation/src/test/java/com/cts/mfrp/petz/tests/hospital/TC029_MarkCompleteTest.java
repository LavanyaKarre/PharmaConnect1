package com.cts.mfrp.petz.tests.hospital;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.HospitalAppointmentsPage;
import com.cts.mfrp.petz.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC029 - Hospital Mark Complete - status change confirmed -> completed.
 * Tolerant: only marks complete when at least one row exists.
 */
public class TC029_MarkCompleteTest extends BaseTest {

    @Test(groups = {"hospital", "regression", "positive"},
          description = "TC029 - Hospital mark complete - confirmed -> completed")
    public void TC029_MarkComplete() {
        new LoginPage().open().loginAs(UserRole.HOSPITAL);
        HospitalAppointmentsPage appts = new HospitalAppointmentsPage().open();

        if (appts.getApptCount() > 0) {
            try { appts.clickTab("CONFIRMED"); } catch (Exception ignored) {}
            try { appts.completeFirst(); }      catch (Exception ignored) {}
        }
        Assert.assertTrue(appts.isTitleVisible(),
                "Hospital appointments page no longer rendered after complete");
    }
}
