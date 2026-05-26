package com.cts.mfrp.petz.tests.hospital;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.ManageDoctorsPage;
import com.cts.mfrp.petz.utils.XmlDataProvider;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * TC026 - Add Doctor validation (name BVA, phone EP, specialization Monkey, slot BVA).
 * Data-driven from hospital-data.xml::doctors.
 */
public class TC026_AddDoctorTest extends BaseTest {

    @Test(dataProvider = "doctorRows",
          groups = {"hospital", "regression", "bva", "ep", "monkey"},
          description = "TC026 - Add Doctor validation - name / phone / specialization / slot duration")
    public void TC026_AddDoctor(Map<String, String> row) {
        String caseId   = row.getOrDefault("caseId", "?");
        String field    = row.getOrDefault("field", "fullName");
        String value    = row.getOrDefault("value", "");
        String expected = row.getOrDefault("expected", "DISABLED");

        new LoginPage().open().loginAs(UserRole.HOSPITAL);
        ManageDoctorsPage doctors = new ManageDoctorsPage().open();
        try { doctors.clickAddDoctor(); } catch (Exception ignored) {}

        try {
            if ("fullName".equalsIgnoreCase(field))            { doctors.fillFullName(value);          doctors.fillSpecialization("General"); doctors.fillPhone("9876543210"); doctors.fillSlotDuration("30"); }
            else if ("phone".equalsIgnoreCase(field))          { doctors.fillFullName("Dr. Smith");    doctors.fillSpecialization("General"); doctors.fillPhone(value);        doctors.fillSlotDuration("30"); }
            else if ("slotDuration".equalsIgnoreCase(field))   { doctors.fillFullName("Dr. Smith");    doctors.fillSpecialization("General"); doctors.fillPhone("9876543210"); doctors.fillSlotDuration(value); }
            else if ("specialization".equalsIgnoreCase(field)) { doctors.fillFullName("Dr. Smith");    doctors.fillSpecialization(value);     doctors.fillPhone("9876543210"); doctors.fillSlotDuration("30"); }
            else                                               { doctors.fillFullName("Dr. Smith");    doctors.fillSpecialization("General"); doctors.fillPhone("9876543210"); doctors.fillSlotDuration("30"); }
        } catch (Exception ignored) {}

        boolean disabled         = doctors.isSaveDisabled();
        boolean expectedDisabled = "DISABLED".equalsIgnoreCase(expected);
        Assert.assertEquals(disabled, expectedDisabled,
                "[" + caseId + "] Save Doctor expected " + expected
                + " but was " + (disabled ? "DISABLED" : "ENABLED"));
    }

    @DataProvider(name = "doctorRows")
    public Object[][] doctorRows() {
        List<Map<String, String>> rows = XmlDataProvider.readSection(AppConstants.HOSPITAL_DATA_XML, "doctors");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) data[i][0] = rows.get(i);
        return data;
    }
}
