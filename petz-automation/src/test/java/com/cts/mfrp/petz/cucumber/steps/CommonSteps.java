package com.cts.mfrp.petz.cucumber.steps;

import com.cts.mfrp.petz.base.DriverFactory;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

/**
 * Cross-cutting steps reusable by any feature (URL assertions etc.).
 * Reads the driver straight from DriverFactory — no shared state needed.
 */
public class CommonSteps {

    private WebDriver driver() {
        return DriverFactory.getDriver();
    }

    @Then("I should be on the {string} page")
    public void iShouldBeOnThePage(String path) {
        String url = driver().getCurrentUrl();
        Assert.assertTrue(url.contains(path),
                "Expected URL to contain '" + path + "' but was '" + url + "'");
    }

    @Then("I should land on the login or dashboard page")
    public void iShouldLandOnLoginOrDashboard() {
        String url = driver().getCurrentUrl();
        Assert.assertTrue(url.contains("/auth/login") || url.contains("/dashboard"),
                "After register expected /auth/login or /dashboard, got " + url);
    }
}
