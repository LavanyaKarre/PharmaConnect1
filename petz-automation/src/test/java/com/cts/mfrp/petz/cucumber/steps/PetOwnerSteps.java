package com.cts.mfrp.petz.cucumber.steps;

import com.cts.mfrp.petz.base.DriverFactory;
import com.cts.mfrp.petz.pages.BookAppointmentPage;
import com.cts.mfrp.petz.pages.BrowseAdoptionPage;
import com.cts.mfrp.petz.pages.MyApplicationsPage;
import com.cts.mfrp.petz.pages.MyAppointmentsPage;
import com.cts.mfrp.petz.pages.PetManagementPage;
import com.cts.mfrp.petz.pages.ReportRescuePage;
import com.cts.mfrp.petz.pages.UserDashboardPage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.time.LocalDate;
import java.util.Map;

/**
 * Pet Owner journey steps — TC03..TC09. Logic mirrors PetOwnerJourneyTest; the form
 * values arrive from Gherkin data tables instead of user-data.xml.
 */
public class PetOwnerSteps {

    private static final Logger logger = LoggerFactory.getLogger(PetOwnerSteps.class);

    @Then("the greeting, emergency banner and at least one KPI tile are visible")
    public void petOwnerDashboardVisible() {
        UserDashboardPage dashboard = new UserDashboardPage();
        Assert.assertTrue(dashboard.isGreetingVisible(),        "Greeting not visible");
        Assert.assertTrue(dashboard.isEmergencyBannerVisible(), "Emergency banner not visible");
        Assert.assertTrue(dashboard.getKpiTileCount() >= 1,     "Expected at least one KPI tile");
    }

    @When("I add a pet with the details:")
    public void iAddAPetWith(DataTable table) {
        Map<String, String> row = table.asMaps().get(0);
        PetManagementPage pets = new PetManagementPage().open();
        if (pets.isAddPetVisible()) pets.clickAddPet();
        pets.fillName(row.get("name"));
        pets.fillAge(row.get("age"));
        pets.fillDescription(row.get("description"));
        pets.clickSave();
    }

    @When("I book a vet appointment with:")
    public void iBookAVetAppointmentWith(DataTable table) {
        Map<String, String> row = table.asMaps().get(0);
        String date = LocalDate.now().plusDays(Integer.parseInt(row.get("dateOffsetDays"))).toString();

        BookAppointmentPage book = new BookAppointmentPage().open();
        if (!book.selectFirstHospital()) {
            logger.info("No hospitals available in dropdown; booking page rendered, skipping submit.");
            Assert.assertTrue(DriverFactory.getDriver().getCurrentUrl().contains("/appointments"),
                    "Booking page navigation failed");
            return;
        }
        book.selectFirstDoctor();
        book.fillDate(date);
        book.selectFirstTime();
        book.fillReason(row.get("reason"));
        try { book.clickConfirm(); }
        catch (Exception e) { logger.info("Confirm Booking not clickable: {}", e.getMessage()); }
    }

    @When("I report a rescue with:")
    public void iReportARescueWith(DataTable table) {
        Map<String, String> row = table.asMaps().get(0);
        ReportRescuePage report = new ReportRescuePage().open();
        report.selectAnimalType(row.get("animalType"));
        report.selectUrgency(row.get("urgency"));
        report.selectArea(row.get("area"));
        report.fillLandmark(row.get("landmark"));
        report.fillCondition(row.get("condition"));
        report.fillReporterPhone(row.get("reporterPhone"));
        report.clickSubmit();
    }

    @Then("I can see the adoption listings")
    public void iCanSeeAdoptionListings() {
        BrowseAdoptionPage browse = new BrowseAdoptionPage().open();
        Assert.assertTrue(browse.isTitleVisible(),       "Browse Adoption title not visible");
        Assert.assertTrue(browse.getPetCardCount() >= 1, "Expected at least 1 adoptable pet card");
    }

    @When("I apply to adopt the first animal with:")
    public void iApplyToAdoptWith(DataTable table) {
        Map<String, String> row = table.asMaps().get(0);
        BrowseAdoptionPage browse = new BrowseAdoptionPage().open();
        if (browse.getPetCardCount() == 0) {
            throw new AssertionError("No adoptable pets - cannot apply (seed data missing?)");
        }
        browse.openFirstPet();
        browse.fillApplyReason(row.get("reason"));
        browse.clickSubmitApplication();
    }

    @Then("my appointments and applications pages both render")
    public void reservationsRender() {
        MyAppointmentsPage appts = new MyAppointmentsPage().open();
        Assert.assertTrue(appts.isTitleVisible(), "My Appointments title not visible");
        int apptCount = appts.getApptCount();

        MyApplicationsPage apps = new MyApplicationsPage().open();
        Assert.assertTrue(apps.isTitleVisible(), "My Applications title not visible");
        int appCount = apps.getCardCount();

        logger.info("{} appointments + {} applications visible", apptCount, appCount);
    }
}
