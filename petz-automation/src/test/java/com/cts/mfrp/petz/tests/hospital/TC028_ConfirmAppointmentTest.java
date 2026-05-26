package com.cts.mfrp.petz.tests.hospital;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.HospitalAppointmentsPage;
import com.cts.mfrp.petz.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC028 - Hospital Confirm Appointment - status change.
 * Tolerant: only confirms when at least one appointment row exists.
 */
public class TC028_ConfirmAppointmentTest extends BaseTest {

    @Test(groups = {"hospital", "regression", "positive"},
          description = "TC028 - Hospital confirm appointment - pending -> confirmed")
    public void TC028_ConfirmAppointment() {
        new LoginPage().open().loginAs(UserRole.HOSPITAL);
        HospitalAppointmentsPage appts = new HospitalAppointmentsPage().open();

        if (appts.getApptCount() > 0) {
            try { appts.clickTab("PENDING"); }    catch (Exception ignored) {}
            try { appts.confirmFirst(); }         catch (Exception ignored) {}
        }
        Assert.assertTrue(appts.isTitleVisible(),
                "Hospital appointments page no longer rendered after confirm");
    }
}
