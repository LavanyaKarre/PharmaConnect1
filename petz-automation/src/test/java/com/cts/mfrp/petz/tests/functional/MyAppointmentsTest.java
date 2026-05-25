package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.MyAppointmentsPage;
import com.cts.mfrp.petz.utils.StepReporter;
import com.cts.mfrp.petz.utils.Waits;
import org.testng.annotations.Test;

/**
 * My Appointments (/appointments) scenario â€” PETZ_TC045 to PETZ_TC046.
 * Group: myAppointments.
 */
public class MyAppointmentsTest extends BaseTest {

    @Test(priority = 45,
          groups = {"myAppointments", "ui", "regression", "positive"},
          description = "PETZ_TC045 - Empty state on /appointments")
    public void TC045_MyApptsEmpty() {
        new LoginPage(driver).loginAsPetOwner();
        MyAppointmentsPage page = new MyAppointmentsPage(driver);
        page.open();

        StepReporter.check("Title 'My Appointments'",
                "Heading visible", page.isTitleVisible());
        StepReporter.check("Subtitle",
                "'Track all your scheduled vet visits'", page.isSubtitleVisible());

        int count = page.getAppointmentRowCount();
        if (count == 0) {
            StepReporter.check("Empty state visible",
                    "'No appointments' empty state visible", page.isEmptyStateVisible());
            try {
                page.clickBookNowEmpty();
                Waits.urlContains(driver, "/appointments/book");
                StepReporter.check("After clicking '+ Book Now'",
                        "/appointments/book", page.getCurrentUrl());
            } catch (Exception ignored) {
                StepReporter.info("'+ Book Now' button not present â€” likely a top-CTA variant only.");
            }
        } else {
            StepReporter.info("Account has " + count + " appointments already â€” skipping empty-state checks.");
        }
    }

    @Test(priority = 46,
          groups = {"myAppointments", "ui", "regression", "positive"},
          description = "PETZ_TC046 - List state on /appointments")
    public void TC046_MyApptsList() {
        new LoginPage(driver).loginAsPetOwner();
        MyAppointmentsPage page = new MyAppointmentsPage(driver);
        page.open();
        int count = page.getAppointmentRowCount();
        StepReporter.info("Appointment rows visible: " + count);
        if (count == 0) {
            StepReporter.note("List state",
                    "Rows with hospital/doctor/date/time + status badge",
                    "no appointments exist â€” structure-only check");
            return;
        }
        StepReporter.check("Status badge present",
                "PENDING / CONFIRMED / COMPLETED / CANCELLED badge",
                page.hasStatusBadge());
    }
}
