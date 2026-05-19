package com.cts.mfrp.petz.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.testng.ITestResult;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.cts.mfrp.petz.constants.AppConstants.REPORT_PATH;
import static com.cts.mfrp.petz.constants.AppConstants.BASE_URL;
import static com.cts.mfrp.petz.constants.AppConstants.API_BASE_URL;

/**
 * Owns the lifecycle of the shared ExtentReports HTML output. The report
 * captures every TestNG @Test as its own entry, tagged with scenario
 * (category), test type (group), author (where set on the test class) and
 * description. Step-level Expected/Actual rows are rendered by
 * {@link StepReporter}.
 */
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
        spark.config().setReportName("PETZ Functional & API Test Run");
        spark.config().setTimeStampFormat("dd-MMM-yyyy HH:mm:ss");
        spark.config().setEncoding("UTF-8");

        extent = new ExtentReports();
        extent.attachReporter(spark);

        extent.setSystemInfo("Project",        "PETZ - Animal Welfare Platform");
        extent.setSystemInfo("Frontend URL",   BASE_URL);
        extent.setSystemInfo("Backend URL",    API_BASE_URL);
        extent.setSystemInfo("Test Plan",      "PETZ_Test_Plan_v5.xlsx (TC001 - TC1101)");
        extent.setSystemInfo("Run Started",    LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss")));
        extent.setSystemInfo("Tester",         System.getProperty("user.name", "Automation QA"));
        extent.setSystemInfo("OS",             System.getProperty("os.name") + " " +
                                                System.getProperty("os.version"));
        extent.setSystemInfo("Java Version",   System.getProperty("java.version"));
        extent.setSystemInfo("Browser",        System.getProperty("browser", "Chrome"));
        extent.setSystemInfo("Selenium",       "4.18.1");
        extent.setSystemInfo("TestNG",         "7.9.0");
        extent.setSystemInfo("REST Assured",   "5.4.0");
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
