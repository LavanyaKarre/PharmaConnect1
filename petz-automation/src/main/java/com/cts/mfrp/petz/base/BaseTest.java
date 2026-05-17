package com.cts.mfrp.petz.base;

import com.cts.mfrp.petz.utils.DriverFactory;
import com.cts.mfrp.petz.utils.TestListener;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;

/**
 * Owns the per-test WebDriver lifecycle.
 * Extent reporting (init, per-test entry, screenshot on failure, flush) is handled
 * by TestListener — no reporting code lives here anymore.
 */
@Listeners(TestListener.class)
public class BaseTest {

    protected WebDriver driver;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverFactory.initDriver();
        driver = DriverFactory.getDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
