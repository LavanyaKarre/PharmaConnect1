package com.cts.mfrp.petz.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import com.cts.mfrp.petz.constants.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton holder for the run-scope ExtentReports instance + a ThreadLocal
 * for the currently-executing test entry. TestListener drives the lifecycle.
 */
public class ExtentReportManager {

    private static final Logger logger = LoggerFactory.getLogger(ExtentReportManager.class);
    private static final String REPORT_PATH = "test-output/reports/ExtentReport.html";

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();

    public static synchronized ExtentReports get() {
        if (extent != null) return extent;

        new File("test-output/reports").mkdirs();
        new File("test-output/screenshots").mkdirs();

        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("PETZ Automation Report");
        spark.config().setReportName("PETZ E2E - 15 Test Cases (Selenium 4 + TestNG)");
        spark.config().setTimeStampFormat("dd-MMM-yyyy HH:mm:ss");
        spark.config().setEncoding("UTF-8");
        // Bundle CSS/JS into the HTML so the report renders correctly even with no
        // internet / blocked CDN. Combined with base64 screenshots (see TestListener),
        // the report is a single self-contained file you can open or share anywhere.
        spark.config().setOfflineMode(true);

        extent = new ExtentReports();
        extent.attachReporter(spark);

        extent.setSystemInfo("Project",     "PETZ - Animal Welfare Platform");
        extent.setSystemInfo("Base URL",    AppConstants.BASE_URL);
        extent.setSystemInfo("Browser",     System.getProperty("browser", "Chrome"));
        extent.setSystemInfo("Headless",    System.getProperty("headless", "false"));
        extent.setSystemInfo("Selenium",    "4.18.1");
        extent.setSystemInfo("TestNG",      "7.9.0");
        extent.setSystemInfo("OS",          System.getProperty("os.name") + " " + System.getProperty("os.version"));
        extent.setSystemInfo("Java",        System.getProperty("java.version"));
        extent.setSystemInfo("Tester",      System.getProperty("user.name", "Automation QA"));
        extent.setSystemInfo("Run Started", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")));

        logger.info("ExtentReports initialised - output: {}", REPORT_PATH);
        return extent;
    }

    public static void setTest(ExtentTest test) { currentTest.set(test); }
    public static ExtentTest getTest() { return currentTest.get(); }
    public static void removeTest() { currentTest.remove(); }

    /**
     * Logs an info step to the test currently in scope, if any. Safe to call from
     * shared code (e.g. BasePage) — it no-ops when no Extent test is active, so the
     * same Page Objects work unchanged under Cucumber where Extent isn't driving.
     */
    public static void logStep(String message) {
        ExtentTest test = currentTest.get();
        if (test != null) test.info(message);
    }

    public static void flush() {
        if (extent != null) {
            extent.flush();
            logger.info("ExtentReports flushed - open {} to view", REPORT_PATH);
        }
    }

    private ExtentReportManager() {}
}
