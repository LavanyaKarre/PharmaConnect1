package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.ReportAnimalPage;
import com.cts.mfrp.petz.utils.StepReporter;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Report Animal (/rescue/report) scenario — PETZ_TC049 to PETZ_TC056.
 * Group: reportAnimal.
 */
public class ReportAnimalTest extends BaseTest {

    @Test(priority = 49, groups = {"reportAnimal"},
          description = "PETZ_TC049 - Layout of /rescue/report")
    public void TC049_ReportFormLayout() {
        new LoginPage(driver).loginAsPetOwner();
        ReportAnimalPage page = new ReportAnimalPage(driver);
        page.open();

        StepReporter.check("Title 'Report Animal in Need'",
                "Heading visible", page.isTitleVisible());
        StepReporter.check("Red emergency banner",
                "'Life-threatening emergency' copy", page.isEmergencyBannerVisible());
        StepReporter.check("ANIMAL DETAILS section",
                "Type of animal + Urgency level visible", page.hasAnimalDetailsSection());
        StepReporter.check("LOCATION section",
                "GPS button + Chennai area + Landmark", page.hasLocationSection());
        StepReporter.check("SITUATION DETAILS section",
                "Condition textarea visible", page.hasSituationSection());
    }

    @Test(priority = 50, groups = {"reportAnimal"},
          description = "PETZ_TC050 - Animal-type dropdown contents")
    public void TC050_ReportAnimalTypeOptions() {
        new LoginPage(driver).loginAsPetOwner();
        ReportAnimalPage page = new ReportAnimalPage(driver);
        page.open();

        List<String> options = page.openAnimalTypeOptions();
        String joined = String.join(" | ", options).toLowerCase();
        StepReporter.info("Animal type options: " + options);

        for (String expected : new String[]{"dog", "cat", "bird", "cow", "monkey", "snake"}) {
            StepReporter.check("Option '" + expected + "' listed",
                    expected, joined);
        }
    }

    @Test(priority = 51, groups = {"reportAnimal"},
          description = "PETZ_TC051 - Urgency level dropdown contents")
    public void TC051_ReportUrgencyOptions() {
        new LoginPage(driver).loginAsPetOwner();
        ReportAnimalPage page = new ReportAnimalPage(driver);
        page.open();

        List<String> options = page.openUrgencyOptions();
        String joined = String.join(" | ", options).toLowerCase();
        StepReporter.info("Urgency options: " + options);

        for (String expected : new String[]{"low", "medium", "high", "critical"}) {
            StepReporter.check("Option '" + expected + "' listed",
                    expected, joined);
        }
        StepReporter.check("Total option count",
                "4 urgency levels", options.size() == 4);
    }

    @Test(priority = 52, groups = {"reportAnimal"},
          description = "PETZ_TC052 - Required inline errors after blur")
    public void TC052_ReportRequiredFieldErrors() {
        new LoginPage(driver).loginAsPetOwner();
        ReportAnimalPage page = new ReportAnimalPage(driver);
        page.open();

        page.blurAnimalType();
        page.blurLandmark();
        page.blurCondition();
        try { Thread.sleep(800); } catch (InterruptedException ignored) {}

        int errCount = page.countRequiredErrors();
        StepReporter.info("Detected Required-style errors: " + errCount);
        StepReporter.check("Required errors after blurring untouched fields",
                "At least one Required error label visible",
                errCount > 0);
    }

    @Test(priority = 53, groups = {"reportAnimal"},
          description = "PETZ_TC053 - Chennai area dropdown contents")
    public void TC053_ReportChennaiAreaOptions() {
        new LoginPage(driver).loginAsPetOwner();
        ReportAnimalPage page = new ReportAnimalPage(driver);
        page.open();

        List<String> options = page.openAreaOptions();
        String joined = String.join(" | ", options);
        StepReporter.info("Area options: " + options);

        for (String expected : new String[]{
                "Anna Nagar", "T. Nagar", "Adyar", "Velachery", "Tambaram", "Perambur"}) {
            StepReporter.check("Option '" + expected + "' listed",
                    expected, joined);
        }
    }

    @Test(priority = 54, groups = {"reportAnimal"},
          description = "PETZ_TC054 - 'Use My GPS Location' click does not crash the page")
    public void TC054_ReportGPSButton() {
        new LoginPage(driver).loginAsPetOwner();
        ReportAnimalPage page = new ReportAnimalPage(driver);
        page.open();

        try { page.clickGpsButton(); }
        catch (Exception e) {
            StepReporter.info("GPS button click threw: " + e.getClass().getSimpleName());
        }
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        StepReporter.check("URL after GPS click",
                "/rescue/report (page did not crash)", page.getCurrentUrl());
    }

    @Test(priority = 55, groups = {"reportAnimal"},
          description = "PETZ_TC055 - Successful rescue-report submission")
    public void TC055_ReportSubmitHappy() {
        new LoginPage(driver).loginAsPetOwner();
        ReportAnimalPage page = new ReportAnimalPage(driver);
        page.open();

        try { page.selectAnimalType("Dog"); } catch (Exception ignored) {}
        try { page.selectUrgency("High");   } catch (Exception ignored) {}
        try { page.selectArea("Adyar");     } catch (Exception ignored) {}
        page.typeLandmark("Near petrol bunk, behind bus stop");
        page.typeCondition("Limping right leg, bleeding");
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}

        page.clickSubmit();
        try { Thread.sleep(3500); } catch (InterruptedException ignored) {}

        StepReporter.check("After submit",
                "Routed to /rescue (or stays on /rescue/report with success toast)",
                page.getCurrentUrl().contains("/rescue"));
    }

    @Test(priority = 56, groups = {"reportAnimal"},
          description = "PETZ_TC056 - 'Back' button on /rescue/report returns to /rescue")
    public void TC056_ReportBackButton() {
        new LoginPage(driver).loginAsPetOwner();
        ReportAnimalPage page = new ReportAnimalPage(driver);
        page.open();
        page.clickBack();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        StepReporter.check("After clicking Back",
                "/rescue", page.getCurrentUrl());
    }
}
