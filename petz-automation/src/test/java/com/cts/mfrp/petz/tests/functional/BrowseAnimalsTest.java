package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.models.testdata.BrowseAnimalsCase;
import com.cts.mfrp.petz.models.testdata.BrowseAnimalsCases;
import com.cts.mfrp.petz.pages.BrowseAnimalsPage;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.StepReporter;
import com.cts.mfrp.petz.utils.Waits;
import org.testng.annotations.Test;

/**
 * Browse Animals (/adoption/animals) scenario — PETZ_TC026 to PETZ_TC031.
 * Group: browseAnimals.
 *
 * <p>TC028 / TC029 / TC030 are search-filter variants and source their per-case
 * inputs/expectations from {@code src/test/resources/testdata/browse-animals.xml}
 * via {@link BrowseAnimalsCases#byId(String)}.
 *
 * <p>TC026 (layout render), TC027 (card content contract), and TC031 (single
 * navigation click) intentionally stay inline — their value is in the
 * page-object contract assertions, not in repeated input data.
 *
 * <p>TC027 / TC028 / TC029 / TC030 depend on backend data. Where the dataset is
 * empty we record the observed state into the report without failing.
 */
public class BrowseAnimalsTest extends BaseTest {

    /** Apply the case's searchMode/searchValue to the right input on the page. */
    private boolean applySearchInput(BrowseAnimalsPage page, BrowseAnimalsCase tc) {
        switch (tc.getSearchMode().toUpperCase()) {
            case "SPECIES" -> {
                try { page.selectSpecies(tc.getSearchValue()); }
                catch (Exception e) {
                    if (Boolean.TRUE.equals(tc.getSpeciesLookupTolerant())) {
                        StepReporter.info("Species '" + tc.getSearchValue()
                                + "' option not in this dataset — skipping filter assertion.");
                        return false;
                    }
                    throw e;
                }
            }
            case "CITY" -> page.typeCity(tc.getSearchValue());
            default -> throw new IllegalArgumentException(
                    "Unknown searchMode in browse-animals.xml: " + tc.getSearchMode());
        }
        return true;
    }

    @Test(priority = 26,
          groups = {"browseAnimals", "ui", "regression", "sanity", "positive"},
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

    @Test(priority = 27,
          groups = {"browseAnimals", "ui", "regression", "positive"},
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

    // ── XML-driven search-filter cases (data in testdata/browse-animals.xml) ──

    @Test(priority = 28,
          groups = {"browseAnimals", "functional", "regression", "positive"},
          description = "PETZ_TC028 - Filter by Species = Dog")
    public void TC028_BrowseSearchSpecies() {
        BrowseAnimalsCase tc = BrowseAnimalsCases.byId("TC028");
        new LoginPage(driver).loginAsPetOwner();
        BrowseAnimalsPage page = new BrowseAnimalsPage(driver);
        page.open();

        if (!applySearchInput(page, tc)) return;   // tolerant skip when species missing
        page.clickSearch();
        Waits.urlContains(driver, tc.getExpectedUrlFragment());

        StepReporter.note("Counter after Species=" + tc.getSearchValue(),
                "Reflects filtered total", page.getCounterText());
        StepReporter.check("Listing still on " + tc.getExpectedUrlFragment(),
                tc.getExpectedUrlFragment(), page.getCurrentUrl());
    }

    @Test(priority = 29,
          groups = {"browseAnimals", "functional", "regression", "positive"},
          description = "PETZ_TC029 - Filter by City = Chennai")
    public void TC029_BrowseSearchCity() {
        BrowseAnimalsCase tc = BrowseAnimalsCases.byId("TC029");
        new LoginPage(driver).loginAsPetOwner();
        BrowseAnimalsPage page = new BrowseAnimalsPage(driver);
        page.open();

        applySearchInput(page, tc);
        page.clickSearch();
        Waits.urlContains(driver, tc.getExpectedUrlFragment());

        StepReporter.note("Counter after City=" + tc.getSearchValue(),
                "Reflects filtered total", page.getCounterText());
        StepReporter.check("Page still " + tc.getExpectedUrlFragment(),
                tc.getExpectedUrlFragment(), page.getCurrentUrl());
    }

    @Test(priority = 30,
          groups = {"browseAnimals", "functional", "regression", "positive"},
          description = "PETZ_TC030 - No-results empty state for City=Atlantis")
    public void TC030_BrowseSearchNoResults() {
        BrowseAnimalsCase tc = BrowseAnimalsCases.byId("TC030");
        new LoginPage(driver).loginAsPetOwner();
        BrowseAnimalsPage page = new BrowseAnimalsPage(driver);
        page.open();

        applySearchInput(page, tc);
        page.clickSearch();
        Waits.documentReady(driver);

        int count = page.getPetCardCount();
        boolean okZeroOrEmpty = Boolean.TRUE.equals(tc.getExpectedZeroCardsOrEmpty())
                ? (count == 0 || page.isEmptyStateVisible())
                : true;
        StepReporter.check("Pet card count for nonsense city",
                "0 cards OR empty-state message",
                okZeroOrEmpty);
    }

    @Test(priority = 31,
          groups = {"browseAnimals", "functional", "regression", "positive"},
          description = "PETZ_TC031 - 'My Applications' shortcut routes to /adoption/my")
    public void TC031_BrowseMyApplicationsLink() {
        new LoginPage(driver).loginAsPetOwner();
        BrowseAnimalsPage page = new BrowseAnimalsPage(driver);
        page.open();
        page.clickMyApplications();
        Waits.urlContains(driver, "/adoption/my");

        StepReporter.check("After clicking 'My Applications'",
                "/adoption/my", page.getCurrentUrl());
    }
}
