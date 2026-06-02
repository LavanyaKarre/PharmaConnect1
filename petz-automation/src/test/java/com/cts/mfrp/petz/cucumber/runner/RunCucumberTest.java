package com.cts.mfrp.petz.cucumber.runner;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

/**
 * Entry point for the Cucumber BDD suite. Listed in testng-cucumber.xml and run by
 * surefire when the "cucumber" Maven profile is active (mvn verify -Pcucumber).
 *
 * Reporting (kept deliberately simple — no Extent adapter):
 *   - "pretty"  : readable console output
 *   - "html"    : self-contained target/cucumber-report.html with steps + embedded
 *                 (base64) screenshots attached in Hooks.afterScenario
 *   - "json"    : target/cucumber.json — consumed by maven-cucumber-reporting (the
 *                 "cucumber" profile) to build the target/cucumber-html-reports dashboard
 *
 * Scenarios run sequentially (the default), which preserves the journey narrative and
 * keeps one shared browser session alive across scenarios — see Hooks / AuthSteps.
 */
@CucumberOptions(
        features = "classpath:features",
        glue     = {
                "com.cts.mfrp.petz.cucumber.hooks",
                "com.cts.mfrp.petz.cucumber.steps"
        },
        plugin   = {
                "pretty",
                "html:target/cucumber-report.html",
                "json:target/cucumber.json"
        },
        monochrome = true
)
public class RunCucumberTest extends AbstractTestNGCucumberTests {
    // Intentionally empty: AbstractTestNGCucumberTests provides the @Test data provider
    // that feeds every scenario to TestNG. We keep the default (sequential) execution.
}
