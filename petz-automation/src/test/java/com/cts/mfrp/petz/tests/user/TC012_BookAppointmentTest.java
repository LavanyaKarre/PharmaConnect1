package com.cts.mfrp.petz.tests.user;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.BookAppointmentPage;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.XmlDataProvider;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * TC012 - Book Appointment - date + reason validation.
 * Data-driven from user-data.xml::appointmentBooking.
 */
public class TC012_BookAppointmentTest extends BaseTest {

    @Test(dataProvider = "apptRows",
          groups = {"user", "regression", "bva", "monkey"},
          description = "TC012 - Book Appointment validation - date BVA + reason BVA/Monkey")
    public void TC012_BookAppointment(Map<String, String> row) {
        String caseId   = row.getOrDefault("caseId", "?");
        String field    = row.getOrDefault("field", "reason");
        String value    = row.getOrDefault("value", "");
        String expected = row.getOrDefault("expected", "DISABLED");

        new LoginPage().open().loginAs(UserRole.PET_OWNER);
        BookAppointmentPage book = new BookAppointmentPage().open();

        try {
            if ("date".equalsIgnoreCase(field))        { book.fillDate(value);        book.fillReason("Routine check"); }
            else if ("reason".equalsIgnoreCase(field)) { book.fillDate(futureDate()); book.fillReason(value); }
            else                                       { book.fillDate(futureDate()); book.fillReason("Default reason"); }
        } catch (Exception ignored) {}

        boolean disabled         = book.isConfirmDisabled();
        boolean expectedDisabled = "DISABLED".equalsIgnoreCase(expected);
        Assert.assertEquals(disabled, expectedDisabled,
                "[" + caseId + "] Confirm expected " + expected
                + " but was " + (disabled ? "DISABLED" : "ENABLED"));
    }

    private String futureDate() {
        return LocalDate.now().plusDays(7).toString();
    }

    @DataProvider(name = "apptRows")
    public Object[][] apptRows() {
        List<Map<String, String>> rows = XmlDataProvider.readSection(
                AppConstants.USER_DATA_XML, "appointmentBooking");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) data[i][0] = rows.get(i);
        return data;
    }
}
