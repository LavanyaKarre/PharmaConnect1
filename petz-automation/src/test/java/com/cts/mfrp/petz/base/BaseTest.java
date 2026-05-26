package com.cts.mfrp.petz.base;

import com.cts.mfrp.petz.base.DriverFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Common superclass for every test class.
 * Owns the per-test-method WebDriver lifecycle.
 * ExtentReports integration lives in TestListener (registered via testng.xml).
 */
public abstract class BaseTest {

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DriverFactory.initDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
