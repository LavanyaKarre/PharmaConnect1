package com.cts.mfrp.petz.cucumber.steps;

import com.cts.mfrp.petz.pages.HospitalAppointmentsPage;
import com.cts.mfrp.petz.pages.HospitalDashboardPage;
import com.cts.mfrp.petz.pages.ManageDoctorsPage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.Map;

/**
 * Hospital journey steps — TC13..TC15. Logic mirrors HospitalJourneyTest.
 */
public class HospitalSteps {

    private static final Logger logger = LoggerFactory.getLogger(HospitalSteps.class);

    @Then("I see the hospital dashboard")
    public void hospitalDashboardVisible() {
        HospitalDashboardPage dashboard = new HospitalDashboardPage();
        Assert.assertTrue(dashboard.isTitleVisible(), "Hospital dashboard title not visible");
    }

    @When("I add a doctor with the details:")
    public void iAddADoctorWith(DataTable table) {
        Map<String, String> row = table.asMaps().get(0);
        ManageDoctorsPage doctors = new ManageDoctorsPage().open();
        doctors.clickAddDoctor();
        doctors.fillFullName(row.get("fullName"));
        doctors.fillSpecialization(row.get("specialization"));
        doctors.fillSlotDuration(row.get("slotDuration"));
        doctors.clickSave();
    }

    @Then("the appointments queue renders and I confirm the first one if present")
    public void appointmentsQueueRenders() {
        HospitalAppointmentsPage appts = new HospitalAppointmentsPage().open();
        Assert.assertTrue(appts.isTitleVisible(), "Hospital appointments page title not visible");
        if (appts.getApptCount() > 0) {
            try { appts.confirmFirst(); } catch (Exception ignored) { /* best-effort */ }
        }
        logger.info("{} appointments visible to this hospital", appts.getApptCount());
    }
}
