package com.cts.mfrp.petz.utils;

import com.aventstack.extentreports.ExtentTest;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener that drives Extent reporting end-to-end.
 *
 * Lifecycle:
 *   onStart        -> init report
 *   onTestStart    -> create a new Extent test entry, assign scenario category
 *                     (e.g. "LandingPage"), copy every TestNG group onto the
 *                     test as a tag (so "smoke", "sanity", "regression",
 *                     "functional"/"api" show up in the Extent Tag view), and
 *                     record the test description from @Test(description=).
 *   onTestSuccess  -> mark pass
 *   onTestFailure  -> attach screenshot + throwable
 *   onTestSkipped  -> mark skipped
 *   onFinish       -> flush report
 */
public class TestListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        log.info("Starting suite: {}", context.getName());
        ExtentReportManager.initReport();
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info("> {}.{}",
                result.getTestClass().getRealClass().getSimpleName(),
                result.getMethod().getMethodName());
        ExtentTest et = ExtentReportManager.createTest(result);

        // Scenario category, e.g. LoginPageTest -> LoginPage
        String className = result.getTestClass().getRealClass().getSimpleName();
        String category  = className.endsWith("Test")
                ? className.substring(0, className.length() - 4) : className;
        et.assignCategory(category);

        // Mirror every TestNG group onto the Extent test entry so the report's
        // Tag view supports filtering by smoke / sanity / regression /
        // functional / api as well as the existing scenario tags.
        String[] groups = result.getMethod().getGroups();
        if (groups != null) {
            for (String g : groups) {
                if (g != null && !g.isBlank()) et.assignCategory(g);
            }
        }

        // Capture the FRD-style description so the report's "info" header
        // shows the test plan intent even when steps log Expected/Actual.
        String description = result.getMethod().getDescription();
        if (description != null && !description.isBlank()) {
            et.info("<b>Test plan intent:</b> " + escapeHtml(description));
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log.info("PASS {}", result.getMethod().getMethodName());
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) t.pass("Test passed");
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log.warn("FAIL {} - {}", result.getMethod().getMethodName(),
                result.getThrowable() == null ? "(no throwable)"
                        : result.getThrowable().getMessage());
        ExtentTest t = ExtentReportManager.getTest();
        if (t == null) return;

        WebDriver driver = DriverFactory.getDriver();
        if (driver != null) {
            try {
                String path = ScreenshotUtil.takeScreenshot(driver, result.getName());
                t.addScreenCaptureFromPath(path);
            } catch (Exception e) {
                log.warn("Could not capture screenshot for {}: {}",
                        result.getName(), e.getMessage());
            }
        }
        t.fail(result.getThrowable());
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.info("SKIP {}", result.getMethod().getMethodName());
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) t.skip(result.getThrowable() != null ? result.getThrowable() : new Throwable("Skipped"));
        ExtentReportManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("Finished suite: {} - passed={}, failed={}, skipped={}",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
        ExtentReportManager.flushReport();
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
