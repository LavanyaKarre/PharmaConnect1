package com.cts.mfrp.petz.cucumber.steps;

import com.cts.mfrp.petz.base.DriverFactory;
import com.cts.mfrp.petz.pages.LandingPage;
import com.cts.mfrp.petz.pages.RegisterPage;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import java.time.Duration;
import java.util.Map;

/**
 * Public (anonymous) journey steps — TC01 landing render, TC02 registration.
 */
public class LandingSteps {

    private LandingPage landing;

    @Given("I open the PETZ landing page")
    public void iOpenTheLandingPage() {
        landing = new LandingPage().open();
    }

    @Then("the logo, hero, stats, features and footer are all visible")
    public void allLandingSectionsVisible() {
        Assert.assertTrue(landing.isLogoVisible(),     "Landing logo not visible");
        Assert.assertTrue(landing.isHeroVisible(),     "Hero section not visible");
        Assert.assertTrue(landing.isStatsVisible(),    "Stats strip not visible");
        Assert.assertTrue(landing.isFeaturesVisible(), "Feature cards not visible");
        Assert.assertTrue(landing.isFooterVisible(),   "Footer not visible");
    }

    @Given("I open the registration page")
    public void iOpenTheRegistrationPage() {
        new RegisterPage().open();
    }

    @When("I register a new account with:")
    public void iRegisterANewAccountWith(DataTable table) {
        Map<String, String> row = table.asMaps().get(0);
        // Fresh email each run so we never collide with an existing account.
        String email = "u" + System.currentTimeMillis() + "@petz.com";

        new RegisterPage()
                .fillFullName(row.get("fullName"))
                .fillPhone(row.get("phone"))
                .fillEmail(email)
                .fillPassword(row.get("password"))
                .fillConfirm(row.get("password"))
                .selectAccountType(row.get("accountType"))
                .clickCreate();

        // Registration is async ("Creating account...") — wait for the route to change.
        try {
            new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(15))
                    .until(d -> !d.getCurrentUrl().contains("/auth/register"));
        } catch (Exception ignored) { /* the Then step asserts the outcome */ }
    }
}
