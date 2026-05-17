package com.cts.mfrp.petz.utils;

import com.aventstack.extentreports.ExtentTest;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener that drives Extent reporting end-to-end.
 *
 * Lifecycle:
 *   onStart        -> init report
 *   onTestStart    -> create a new Extent test entry from the @Test method
 *   onTestSuccess  -> mark pass
 *   onTestFailure  -> attach screenshot + throwable
 *   onTestSkipped  -> mark skipped
 *   onFinish       -> flush report
 *
 * No test method needs to know this exists.
 */
public class TestListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        ExtentReportManager.initReport();
    }

    @Override
    public void onTestStart(ITestResult result) {
        com.aventstack.extentreports.ExtentTest et =
                ExtentReportManager.createTest(result);
        String className = result.getTestClass().getRealClass().getSimpleName();
        String category  = className.endsWith("Test")
                ? className.substring(0, className.length() - 4) : className;
        et.assignCategory(category);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) t.pass("Test passed");
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest t = ExtentReportManager.getTest();
        if (t == null) return;

        WebDriver driver = DriverFactory.getDriver();
        if (driver != null) {
            try {
                String path = ScreenshotUtil.takeScreenshot(driver, result.getName());
                t.addScreenCaptureFromPath(path);
            } catch (Exception ignored) { }
        }
        t.fail(result.getThrowable());
        ExtentReportManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) t.skip(result.getThrowable() != null ? result.getThrowable() : new Throwable("Skipped"));
        ExtentReportManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flushReport();
    }
}
