package com.cts.mfrp.petz.utils;

import com.aventstack.extentreports.ExtentTest;
import org.testng.Assert;

/**
 * Logs each test step as a small Expected | Actual | Status table into the
 * current Extent test entry, then asserts the condition so the test fails
 * naturally on mismatch.
 *
 * Use it like this from any @Test method:
 *
 *   StepReporter.check("Open /auth/login",
 *                      "Welcome back heading is visible",
 *                      page.isWelcomeBackVisible());
 *
 *   StepReporter.check("Title",
 *                      "Petz - Animal Welfare Platform",
 *                      driver.getTitle());
 *
 * The helper does two things at once: records the step into the report AND
 * asserts. That keeps test bodies short while making the HTML report easy
 * to scan side-by-side.
 */
public class StepReporter {

    /** Step with a boolean condition. Logs "as expected" or "not as expected" as the actual. */
    public static void check(String stepDescription, String expected, boolean condition) {
        log(stepDescription, expected, condition ? "as expected" : "not as expected", condition);
        Assert.assertTrue(condition,
                "Step failed: " + stepDescription + " | expected: " + expected);
    }

    /** Step with an actual string value compared against an expected (substring, case-insensitive). */
    public static void check(String stepDescription, String expected, String actual) {
        String a = actual == null ? "" : actual;
        boolean pass = a.toLowerCase().contains(expected.toLowerCase())
                       || expected.toLowerCase().contains(a.toLowerCase());
        log(stepDescription, expected, a, pass);
        Assert.assertTrue(pass,
                "Step failed: " + stepDescription + " | expected: " + expected + " | actual: " + a);
    }

    /** Soft info line (no assertion) — for narration like "Submitted form" or skip notes. */
    public static void info(String message) {
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) t.info(esc(message));
    }

    /** Soft step (no assertion) — useful when the spec's expected is informational only. */
    public static void note(String stepDescription, String expected, String actual) {
        log(stepDescription, expected, actual == null ? "" : actual, true);
    }

    // ── internal ──
    private static void log(String stepDescription, String expected, String actual, boolean pass) {
        ExtentTest t = ExtentReportManager.getTest();
        if (t == null) return;
        String color = pass ? "#22c55e" : "#ef4444";
        String row =
                "<table style='border-collapse:collapse;width:100%;font-size:13px;margin:2px 0;'>"
              + "<tr style='background:#1d2939;color:#fff;'>"
              +   "<th style='padding:4px 8px;text-align:left;width:35%;'>Step</th>"
              +   "<th style='padding:4px 8px;text-align:left;width:30%;'>Expected</th>"
              +   "<th style='padding:4px 8px;text-align:left;width:25%;'>Actual</th>"
              +   "<th style='padding:4px 8px;width:10%;'>Status</th></tr>"
              + "<tr style='background:#2b3343;color:#fff;'>"
              +   "<td style='padding:4px 8px;'>" + esc(stepDescription) + "</td>"
              +   "<td style='padding:4px 8px;'>" + esc(expected) + "</td>"
              +   "<td style='padding:4px 8px;'>" + esc(actual) + "</td>"
              +   "<td style='padding:4px 8px;font-weight:bold;color:" + color + ";'>"
              +     (pass ? "PASS" : "FAIL") + "</td>"
              + "</tr></table>";
        if (pass) t.pass(row); else t.fail(row);
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private StepReporter() {}
}
