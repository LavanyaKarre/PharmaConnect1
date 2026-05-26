package com.cts.mfrp.petz.tests.hospital;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.HospitalAppointmentsPage;
import com.cts.mfrp.petz.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC027 - Hospital View Appointments - status tabs visible.
 */
public class TC027_HospitalViewAppointmentsTest extends BaseTest {

    @Test(groups = {"hospital", "regression", "positive"},
          description = "TC027 - Hospital View Appointments shows status tabs")
    public void TC027_HospitalViewAppointments() {
        new LoginPage().open().loginAs(UserRole.HOSPITAL);
        HospitalAppointmentsPage appts = new HospitalAppointmentsPage().open();

        Assert.assertTrue(appts.isTitleVisible(), "Appointments title not visible");
        Assert.assertTrue(appts.getTabCount() >= 2,
                "Expected >=2 status tabs but got " + appts.getTabCount());
    }
}
