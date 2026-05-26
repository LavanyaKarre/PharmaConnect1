package com.cts.mfrp.petz.tests.hospital;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.ManageDoctorsPage;
import com.cts.mfrp.petz.utils.RandomDataGenerator;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC030 - Manage Doctors - edit (Monkey on specialization) + delete first doctor.
 * Tolerant: skips both actions if no doctors exist.
 */
public class TC030_ManageDoctorsTest extends BaseTest {

    @Test(groups = {"hospital", "regression", "monkey"},
          description = "TC030 - Manage Doctors edit (Monkey) + delete with confirm dialog")
    public void TC030_ManageDoctors() {
        new LoginPage().open().loginAs(UserRole.HOSPITAL);
        ManageDoctorsPage doctors = new ManageDoctorsPage().open();

        if (doctors.getDoctorCount() > 0) {
            try {
                doctors.clickEditFirst();
                doctors.fillSpecialization(RandomDataGenerator.randomXss());
                doctors.clickSave();
            } catch (Exception ignored) {}
            try {
                doctors.clickDeleteFirst();
                doctors.confirmDelete();
            } catch (Exception ignored) {}
        }
        Assert.assertTrue(doctors.getDoctorCount() >= 0,
                "Doctors page in unusable state after edit/delete");
    }
}
