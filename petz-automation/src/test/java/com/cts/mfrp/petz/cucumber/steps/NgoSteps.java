package com.cts.mfrp.petz.cucumber.steps;

import com.cts.mfrp.petz.pages.AdoptionApplicationsPage;
import com.cts.mfrp.petz.pages.NGOAnimalsPage;
import com.cts.mfrp.petz.pages.NGODashboardPage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.util.Map;

/**
 * NGO journey steps — TC10..TC12. Logic mirrors NGOJourneyTest.
 */
public class NgoSteps {

    private static final Logger logger = LoggerFactory.getLogger(NgoSteps.class);

    @Then("I see the NGO dashboard tiles and quick actions")
    public void ngoDashboardVisible() {
        NGODashboardPage dashboard = new NGODashboardPage();
        Assert.assertTrue(dashboard.isTitleVisible(),        "NGO dashboard title not visible");
        Assert.assertTrue(dashboard.getStatTileCount() >= 1, "Expected at least one stat tile");
        Assert.assertTrue(dashboard.hasQuickActions(),       "Quick actions not visible");
    }

    @When("I add an animal with the details:")
    public void iAddAnAnimalWith(DataTable table) {
        Map<String, String> row = table.asMaps().get(0);
        NGOAnimalsPage animals = new NGOAnimalsPage().open();
        animals.clickAddAnimal();
        animals.fillName(row.get("name"));
        animals.fillSpecies(row.get("species"));
        animals.fillAge(row.get("age"));
        animals.fillDescription(row.get("description"));
        animals.clickSave();
    }

    @Then("the adoption applications screen renders")
    public void applicationsScreenRenders() {
        AdoptionApplicationsPage apps = new AdoptionApplicationsPage().open();
        Assert.assertTrue(apps.isTitleVisible(), "Applications page title not visible");
        logger.info("{} applications visible to this NGO", apps.getCardCount());
    }
}
