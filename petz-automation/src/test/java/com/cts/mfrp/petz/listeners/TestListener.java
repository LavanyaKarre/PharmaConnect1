package com.cts.mfrp.petz.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.cts.mfrp.petz.base.DriverFactory;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * TestNG listener that:
 *   - Initialises ExtentReports on suite start
 *   - Creates an ExtentTest per @Test method, assigns its groups as categories
 *   - Captures a final-page screenshot on every test (pass or fail)
 *   - Logs pass/fail/skip with embedded screenshot + standalone .png on disk
 *   - Flushes the report on suite end
 *
 * Registered in testng.xml via &lt;listener class-name="..."/&gt;.
 */
public class TestListener implements ITestListener {

    private static final Logger logger = LoggerFactory.getLogger(TestListener.class);
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS");

    @Override
    public void onStart(ITestContext context) {
        ExtentReportManager.get(); // init
        logger.info("=== Suite '{}' started ===", context.getName());
    }

    @Override
    public void onTestStart(ITestResult result) {
        String name = displayName(result);
        String desc = result.getMethod().getDescription();
        ExtentTest test = ExtentReportManager.get().createTest(name, desc == null ? "" : desc);
        for (String group : result.getMethod().getGroups()) test.assignCategory(group);
        // Show data-row parameter if this is a data-driven invocation
        Object[] params = result.getParameters();
        if (params != null && params.length > 0 && params[0] != null) {
            test.info("Data row: " + params[0].toString());
        }
        ExtentReportManager.setTest(test);
        logger.info(">> {}", name);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String shot = captureScreenshot(result, "PASS");
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            if (shot != null) {
                test.pass("Passed",
                        MediaEntityBuilder.createScreenCaptureFromPath(shot).build());
            } else {
                test.pass("Passed");
            }
        }
        ExtentReportManager.removeTest();
        logger.info("PASS: {}", displayName(result));
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String shot = captureScreenshot(result, "FAIL");
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            Throwable err = result.getThrowable();
            String msg = err == null ? "Failed" : firstLine(err.getMessage() == null ? err.toString() : err.getMessage());
            if (shot != null) {
                test.fail("FAILED: " + msg,
                        MediaEntityBuilder.createScreenCaptureFromPath(shot).build());
            } else {
                test.fail("FAILED: " + msg);
            }
            if (err != null) test.fail(err);
        }
        ExtentReportManager.removeTest();
        logger.warn("FAIL: {} - {}", displayName(result),
                result.getThrowable() == null ? "" : firstLine(result.getThrowable().getMessage()));
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentReportManager.getTest();
        if (test != null) {
            String reason = result.getThrowable() == null
                    ? "Skipped (dependency or config failure)"
                    : firstLine(result.getThrowable().getMessage());
            test.skip("SKIPPED: " + reason);
        }
        ExtentReportManager.removeTest();
        logger.info("SKIP: {}", displayName(result));
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportManager.flush();
        logger.info("=== Suite '{}' finished | passed={} failed={} skipped={} ===",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    // ---------- helpers ----------

    private String displayName(ITestResult result) {
        String method = result.getMethod().getMethodName();
        Object[] params = result.getParameters();
        if (params == null || params.length == 0) return method;
        // Append the caseId if the param is a Map containing one
        Object first = params[0];
        if (first instanceof java.util.Map) {
            Object caseId = ((java.util.Map<?, ?>) first).get("caseId");
            if (caseId != null) return method + " [" + caseId + "]";
        }
        return method;
    }

    private String captureScreenshot(ITestResult result, String tag) {
        if (!DriverFactory.hasDriver()) return null;
        try {
            byte[] png = ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BYTES);
            Path dir = Paths.get(AppConstants.SCREENSHOT_DIR);
            Files.createDirectories(dir);
            String filename = sanitize(displayName(result)) + "_" + tag + "_"
                    + LocalDateTime.now().format(STAMP) + ".png";
            Path file = dir.resolve(filename);
            Files.write(file, png);
            return file.toAbsolutePath().toString();
        } catch (Exception e) {
            logger.error("Screenshot capture failed: {}", e.getMessage());
            return null;
        }
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9._\\[\\]-]", "_");
    }

    private static String firstLine(String s) {
        if (s == null) return "";
        int nl = s.indexOf('\n');
        return nl > 0 ? s.substring(0, nl) : s;
    }
}
