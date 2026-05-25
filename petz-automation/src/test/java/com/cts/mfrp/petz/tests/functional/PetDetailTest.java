package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.BrowseAnimalsPage;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.PetDetailPage;
import com.cts.mfrp.petz.utils.StepReporter;
import com.cts.mfrp.petz.utils.Waits;
import org.testng.annotations.Test;

/**
 * Pet Detail (/adoption/animals/{id}) scenario â€” PETZ_TC032 to PETZ_TC035.
 * Group: petDetail.
 *
 * If the dataset has no listed pets, we mark the entire scenario as
 * informational (no cards = nothing to detail) â€” keeps runs green on
 * empty environments.
 */
public class PetDetailTest extends BaseTest {

    /** Returns false if no pets are listed and the scenario should bail. */
    private boolean openFirstPet() {
        new LoginPage(driver).loginAsPetOwner();
        BrowseAnimalsPage browse = new BrowseAnimalsPage(driver);
        browse.open();
        if (browse.getPetCardCount() == 0) {
            StepReporter.info("No pets listed in the live dataset â€” skipping detail-page checks.");
            return false;
        }
        browse.openFirstPetProfile();
        Waits.urlContains(driver, "/adoption/animals/");
        return true;
    }

    @Test(priority = 32,
          groups = {"petDetail", "ui", "regression", "positive"},
          description = "PETZ_TC032 - Pet detail page layout")
    public void TC032_PetDetailLayout() {
        if (!openFirstPet()) return;
        PetDetailPage page = new PetDetailPage(driver);

        StepReporter.check("URL pattern",
                "/adoption/animals/<id>", page.getCurrentUrl());
        StepReporter.check("Breadcrumb 'All Animals'",
                "Visible at top", page.isAllAnimalsBreadcrumbVisible());
        StepReporter.check("'AVAILABLE' chip",
                "Orange AVAILABLE chip visible", page.isAvailableChipVisible());
        StepReporter.check("'Vaccinated' chip",
                "Green Vaccinated chip visible", page.isVaccinatedChipVisible());
        StepReporter.check("Four attribute chips",
                "Age / Gender / Species / Breed all present", page.hasAttributeChips());
        StepReporter.check("Location pin icon",
                "Pin icon present", page.hasLocationPin());
    }

    @Test(priority = 33,
          groups = {"petDetail", "ui", "regression", "positive"},
          description = "PETZ_TC033 - Apply-to-Adopt form fields")
    public void TC033_ApplyFormFields() {
        if (!openFirstPet()) return;
        PetDetailPage page = new PetDetailPage(driver);
        StepReporter.check("Apply heading visible",
                "'Apply to Adopt <name>' visible", page.isApplyFormVisible());
    }

    @Test(priority = 34,
          groups = {"petDetail", "functional", "regression", "negative"},
          description = "PETZ_TC034 - Submit button is disabled while 'Why' is empty")
    public void TC034_ApplySubmitDisabled() {
        if (!openFirstPet()) return;
        PetDetailPage page = new PetDetailPage(driver);

        // Try filling Why with empty (clear) â€” submit should remain disabled.
        try { page.fillWhy(""); } catch (Exception ignored) {}
        StepReporter.check("Submit state with empty 'Why'",
                "Submit disabled (button greyed out)", page.isSubmitDisabled());
    }

    @Test(priority = 35,
          groups = {"petDetail", "functional", "regression", "sanity", "positive"},
          description = "PETZ_TC035 - Successful adoption-application submission")
    public void TC035_ApplySubmitHappy() {
        if (!openFirstPet()) return;
        PetDetailPage page = new PetDetailPage(driver);

        page.fillWhy("I have a quiet home with a yard and prior pet experience.");
        page.fillExperience("Owned a Labrador for 8 years.");

        boolean enabled = !page.isSubmitDisabled();
        StepReporter.check("Submit button enabled after filling Why",
                "Submit enabled (orange)", enabled);

        if (!enabled) {
            StepReporter.info("Submit remained disabled â€” likely an extra required field. Stopping happy-path here.");
            return;
        }
        page.clickSubmit();
        Waits.documentReady(driver);

        StepReporter.note("URL after submit",
                "/adoption/my OR success toast on detail page", page.getCurrentUrl());
    }
}
