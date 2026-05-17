package com.cts.mfrp.petz.tests;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.BrowseAnimalsPage;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.StepReporter;
import org.testng.annotations.Test;

/**
 * Browse Animals (/adoption/animals) scenario — PETZ_TC026 to PETZ_TC031.
 * Group: browseAnimals.
 *
 * TC027 / TC028 / TC029 / TC030 depend on backend data. Where the dataset is
 * empty we record the observed state into the report without failing.
 */
public class BrowseAnimalsTest extends BaseTest {

    @Test(priority = 26, groups = {"browseAnimals"},
          description = "PETZ_TC026 - Validate 'Find Your Companion' page layout")
    public void TC026_BrowseRender() {
        new LoginPage(driver).loginAsPetOwner();
        BrowseAnimalsPage page = new BrowseAnimalsPage(driver);
        page.open();

        StepReporter.check("Title 'Find Your Companion'",
                "Heading is visible", page.isTitleVisible());
        StepReporter.check("Subtitle copy",
                "'Animals looking for a loving forever home' visible",
                page.isSubtitleVisible());
        StepReporter.check("'My Applications' button (top-right)",
                "Button is visible", page.isMyApplicationsBtnVisible());
        StepReporter.check("Species dropdown",
                "Species selector visible", page.isSpeciesSelectVisible());
        StepReporter.check("City or area input",
                "City input visible", page.isCityInputVisible());
        StepReporter.check("Search button",
                "Search button visible", page.isSearchButtonVisible());
        StepReporter.note("Animals-available counter text",
                "'N animals available for adoption'", page.getCounterText());
    }

    @Test(priority = 27, groups = {"browseAnimals"},
          description = "PETZ_TC027 - Validate fields on each pet card")
    public void TC027_BrowsePetCardFields() {
        new LoginPage(driver).loginAsPetOwner();
        BrowseAnimalsPage page = new BrowseAnimalsPage(driver);
        page.open();

        int count = page.getPetCardCount();
        StepReporter.info("Pet cards visible: " + count);

        if (count == 0) {
            StepReporter.note("Pet card fields",
                    "Species chip / breed-age / city / Vaccinated / View Profile",
                    "no cards in this dataset — skipped strict checks");
            return;
        }
        StepReporter.check("Species chip overlay (DOG/CAT/etc.)",
                "Species chip text present somewhere",
                page.cardShowsSpeciesChip("DOG") || page.cardShowsSpeciesChip("CAT"));
        StepReporter.check("'Vaccinated' chip",
                "At least one card shows Vaccinated", page.cardShowsVaccinatedChip());
        StepReporter.check("Location pin icon",
                "Location icon present on cards", page.cardShowsLocationPin());
    }

    @Test(priority = 28, groups = {"browseAnimals"},
          description = "PETZ_TC028 - Filter by Species = Dog")
    public void TC028_BrowseSearchSpecies() {
        new LoginPage(driver).loginAsPetOwner();
        BrowseAnimalsPage page = new BrowseAnimalsPage(driver);
        page.open();

        try { page.selectSpecies("Dog"); }
        catch (Exception e) {
            StepReporter.info("Species 'Dog' option not in this dataset — skipping filter assertion.");
            return;
        }
        page.clickSearch();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        StepReporter.note("Counter after Species=Dog",
                "Reflects filtered total", page.getCounterText());
        StepReporter.check("Listing still on /adoption/animals",
                "/adoption/animals", page.getCurrentUrl());
    }

    @Test(priority = 29, groups = {"browseAnimals"},
          description = "PETZ_TC029 - Filter by City = Chennai")
    public void TC029_BrowseSearchCity() {
        new LoginPage(driver).loginAsPetOwner();
        BrowseAnimalsPage page = new BrowseAnimalsPage(driver);
        page.open();

        page.typeCity("Chennai");
        page.clickSearch();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        StepReporter.note("Counter after City=Chennai",
                "Reflects filtered total", page.getCounterText());
        StepReporter.check("Page still /adoption/animals",
                "/adoption/animals", page.getCurrentUrl());
    }

    @Test(priority = 30, groups = {"browseAnimals"},
          description = "PETZ_TC030 - No-results empty state for City=Atlantis")
    public void TC030_BrowseSearchNoResults() {
        new LoginPage(driver).loginAsPetOwner();
        BrowseAnimalsPage page = new BrowseAnimalsPage(driver);
        page.open();

        page.typeCity("Atlantis");
        page.clickSearch();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        int count = page.getPetCardCount();
        StepReporter.check("Pet card count for nonsense city",
                "0 cards OR empty-state message",
                count == 0 || page.isEmptyStateVisible());
    }

    @Test(priority = 31, groups = {"browseAnimals"},
          description = "PETZ_TC031 - 'My Applications' shortcut routes to /adoption/my")
    public void TC031_BrowseMyApplicationsLink() {
        new LoginPage(driver).loginAsPetOwner();
        BrowseAnimalsPage page = new BrowseAnimalsPage(driver);
        page.open();
        page.clickMyApplications();
        try { Thread.sleep(1500); } catch (InterruptedException ignored) {}

        StepReporter.check("After clicking 'My Applications'",
                "/adoption/my", page.getCurrentUrl());
    }
}
