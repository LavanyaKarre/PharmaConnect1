package com.cts.mfrp.petz.base;

import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Common superclass for every journey test class.
 *
 * Each journey class shares ONE WebDriver across all its @Test methods, so a role
 * logs in once at @BeforeClass and stays logged in for the rest of that class.
 * Browser quits at @AfterClass. Class execution order is fixed by testng.xml.
 *
 * Override {@link #role()} to choose which role auto-logs-in:
 *   return UserRole.PET_OWNER / NGO / HOSPITAL for role chains
 *   return null              for anonymous journeys (landing, register)
 */
public abstract class BaseTest {

    protected WebDriver driver;

    /** Role to auto-login as in @BeforeClass. Return null to skip login. */
    protected abstract UserRole role();

    @BeforeClass(alwaysRun = true)
    public void setUp() {
        DriverFactory.initDriver();
        driver = DriverFactory.getDriver();
        if (role() != null) {
            new LoginPage().open().loginAs(role());
        }
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
