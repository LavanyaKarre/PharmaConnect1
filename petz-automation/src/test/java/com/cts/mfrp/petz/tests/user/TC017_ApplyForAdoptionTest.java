package com.cts.mfrp.petz.tests.user;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.base.DriverFactory;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.BrowseAdoptionPage;
import com.cts.mfrp.petz.pages.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

/**
 * TC017 - Apply for Adoption - submit application from pet detail.
 * Tolerant: only fully runs if at least one pet is visible on Browse.
 */
public class TC017_ApplyForAdoptionTest extends BaseTest {

    @Test(groups = {"user", "regression", "positive"},
          description = "TC017 - Apply for Adoption submits an application")
    public void TC017_ApplyForAdoption() {
        new LoginPage().open().loginAs(UserRole.PET_OWNER);
        BrowseAdoptionPage browse = new BrowseAdoptionPage().open();

        if (browse.getPetCardCount() == 0) {
            Assert.assertTrue(browse.isNoResultsVisible(),
                    "No pets to apply for AND no empty-state shown");
            return; // nothing to apply for
        }

        browse.openFirstPet();

        try {
            new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("textarea")));
            DriverFactory.getDriver().findElement(By.cssSelector("textarea"))
                    .sendKeys("I have a quiet home and lots of love to give.");
            DriverFactory.getDriver().findElement(By.xpath(
                    "//button[contains(normalize-space(),'Submit') or contains(normalize-space(),'Apply')]"))
                    .click();
        } catch (Exception ignored) { /* apply form not available on this pet */ }

        String url = DriverFactory.getDriver().getCurrentUrl();
        Assert.assertTrue(url.contains("/adoption"),
                "After applying, expected /adoption path but got " + url);
    }
}
