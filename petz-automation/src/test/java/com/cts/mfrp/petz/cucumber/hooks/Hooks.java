package com.cts.mfrp.petz.cucumber.hooks;

import com.cts.mfrp.petz.base.DriverFactory;
import io.cucumber.java.AfterAll;
import io.cucumber.java.After;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lifecycle for the Cucumber suite.
 *
 * One browser for the whole run: started once in @BeforeAll, quit once in @AfterAll.
 * The session therefore survives across scenarios, which is what lets us log in only
 * once per journey (AuthSteps logs in only when the requested role changes).
 *
 * @After (per scenario) attaches a screenshot to the report as base64 — same principle
 * as the TestNG ExtentReport: a self-contained report whose images always render.
 */
public class Hooks {

    private static final Logger logger = LoggerFactory.getLogger(Hooks.class);

    @BeforeAll
    public static void beforeAll() {
        DriverFactory.initDriver();
        logger.info("=== Cucumber suite: browser started ===");
    }

    @AfterAll
    public static void afterAll() {
        DriverFactory.quitDriver();
        logger.info("=== Cucumber suite: browser quit ===");
    }

    @After
    public void afterScenario(Scenario scenario) {
        if (!DriverFactory.hasDriver()) return;
        try {
            byte[] png = ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BYTES);
            scenario.attach(png, "image/png", scenario.getName());
        } catch (Exception e) {
            logger.warn("Screenshot attach failed for '{}': {}", scenario.getName(), e.getMessage());
        }
    }
}
