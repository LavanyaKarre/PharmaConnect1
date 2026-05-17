package com.cts.mfrp.petz.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestResult;

import java.io.File;

import static com.cts.mfrp.petz.constants.AppConstants.REPORT_PATH;

public class ExtentReportManager {

    private static ExtentReports extent;
    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public static synchronized void initReport() {
        if (extent != null) return;

        new File("test-output/reports").mkdirs();
        new File("test-output/screenshots").mkdirs();

        ExtentSparkReporter spark = new ExtentSparkReporter(REPORT_PATH);
        spark.config().setTheme(Theme.DARK);
        spark.config().setDocumentTitle("PETZ Automation Report");
        spark.config().setReportName("PETZ UI Test Run");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("Project",     "PETZ Animal Welfare Platform");
        extent.setSystemInfo("Environment", "https://stellular-taffy-e3ee7a.netlify.app");
        extent.setSystemInfo("Tester",      "Automation QA");
        extent.setSystemInfo("Browser",     "Chrome");
    }

    public static ExtentTest createTest(String testName, String description) {
        ExtentTest extentTest = extent.createTest(testName, description);
        test.set(extentTest);
        return extentTest;
    }

    public static ExtentTest createTest(ITestResult result) {
        String name = result.getMethod().getMethodName();
        String desc = result.getMethod().getDescription();
        return createTest(name, desc == null ? "" : desc);
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void removeTest() {
        test.remove();
    }

    public static void flushReport() {
        if (extent != null) extent.flush();
    }
}
