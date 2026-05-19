package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.NGOMyAnimalsPage;
import com.cts.mfrp.petz.utils.StepReporter;
import com.cts.mfrp.petz.utils.Waits;
import org.testng.annotations.Test;

import java.util.List;

/**
 * NGO My Animals (/ngo/animals) scenario â€” PETZ_TC063 to PETZ_TC065.
 * Group: ngoMyAnimals.
 */
public class NGOMyAnimalsTest extends BaseTest {

    @Test(priority = 63,
          groups = {"ngoMyAnimals", "functional", "regression", "positive"},
          description = "PETZ_TC063 - Empty state of /ngo/animals")
    public void TC063_NGOAnimalsEmpty() {
        new LoginPage(driver).loginAsNgo();
        NGOMyAnimalsPage page = new NGOMyAnimalsPage(driver);
        page.open();

        StepReporter.check("Title 'My Animals'",
                "Heading visible", page.isTitleVisible());
        StepReporter.check("Subtitle",
                "'Manage all animals listed for adoption'", page.isSubtitleVisible());
        StepReporter.check("'+ Add Animal' button",
                "Top-right Add Animal button visible", page.isAddAnimalBtnVisible());
        StepReporter.check("Search input",
                "Search by name or breed input visible", page.isSearchInputVisible());
        StepReporter.check("Three filters",
                "Species / Status / Sort filters visible", page.areFiltersVisible());

        if (page.isEmptyStateVisible()) {
            StepReporter.check("Empty state",
                    "'No animals listed yet' card visible", true);
        } else {
            StepReporter.info("NGO already has animals listed â€” skipping empty-state assertion.");
        }
    }

    @Test(priority = 64,
          groups = {"ngoMyAnimals", "functional", "regression", "positive"},
          description = "PETZ_TC064 - Filters: Species / Status / Sort dropdown contents")
    public void TC064_NGOAnimalsFilters() {
        new LoginPage(driver).loginAsNgo();
        NGOMyAnimalsPage page = new NGOMyAnimalsPage(driver);
        page.open();

        List<String> species = page.openSpeciesOptions();
        StepReporter.info("Species options: " + species);
        StepReporter.check("Species dropdown has options",
                "1+ option (including All)", !species.isEmpty());

        List<String> status = page.openStatusOptions();
        StepReporter.info("Status options: " + status);
        StepReporter.check("Status dropdown has options",
                "1+ option (including All)", !status.isEmpty());

        List<String> sort = page.openSortOptions();
        StepReporter.info("Sort options: " + sort);
        StepReporter.check("Sort dropdown has 'Newest First'",
                "Newest First default sort",
                String.join(" ", sort).toLowerCase().contains("newest"));
    }

    @Test(priority = 65,
          groups = {"ngoMyAnimals", "functional", "regression", "positive"},
          description = "PETZ_TC065 - Add Animal CTA opens the new-animal form/dialog")
    public void TC065_NGOAddAnimalCTA() {
        new LoginPage(driver).loginAsNgo();
        NGOMyAnimalsPage page = new NGOMyAnimalsPage(driver);
        page.open();

        try { page.clickAddFirstAnimal(); }
        catch (Exception e) { page.clickAddAnimalTop(); }
        Waits.pageSourceContainsAny(driver, "Species", "Breed", "Add Animal");

        StepReporter.check("Add-animal form rendered",
                "Form with Name / Species / Breed / Age / Gender / City",
                page.isAddAnimalFormVisible() && page.addAnimalFormHasExpectedFields());
    }
}
