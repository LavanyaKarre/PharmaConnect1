package com.cts.mfrp.petz.utils;

import com.aventstack.extentreports.ExtentTest;
import org.testng.Assert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Logs each test step as an Expected | Actual | Status table into the
 * current Extent test entry, then asserts so the test fails naturally on
 * mismatch. Every assertion path renders a row in the report, including
 * the {@link #step(String, String, ThrowingSupplier)} helper which captures
 * Selenium exceptions and writes them into the Actual column instead of
 * letting them short-circuit the report.
 *
 * <p>Comparison rules for the 3-arg string overload:
 * <ol>
 *   <li>Both sides are URL-normalized (protocol + host stripped) so a test
 *       plan that says "Remains on /auth/login" matches the live URL
 *       "https://stellular-taffy-e3ee7a.netlify.app/auth/login".</li>
 *   <li>Placeholders like {@code <id>}, {@code <name>} in the expected
 *       string match any non-empty token in the actual string. Lets the
 *       test plan say "/adoption/animals/&lt;id&gt;" and pass for any id.</li>
 *   <li>Falls back to a case-insensitive bidirectional substring match
 *       so other free-text expected/actual pairs work as before.</li>
 * </ol>
 */
public class StepReporter {

    private static final Pattern URL_PREFIX  = Pattern.compile("(?i)https?://[^/]+");
    private static final Pattern PLACEHOLDER = Pattern.compile("<[^>]+>");

    /** Step with a boolean condition. Logs "as expected" or "not as expected" as the actual. */
    public static void check(String stepDescription, String expected, boolean condition) {
        log(stepDescription, expected, condition ? "as expected" : "not as expected", condition);
        Assert.assertTrue(condition,
                "Step failed: " + stepDescription + " | expected: " + expected);
    }

    /**
     * Step with an actual string value compared against an expected value.
     * URL- and placeholder-aware (see class-level Javadoc for the rules).
     */
    public static void check(String stepDescription, String expected, String actual) {
        String a = actual == null ? "" : actual;
        boolean pass = matches(expected, a);
        log(stepDescription, expected, a, pass);
        Assert.assertTrue(pass,
                "Step failed: " + stepDescription + " | expected: " + expected + " | actual: " + a);
    }

    /**
     * Step that wraps a supplier so any Selenium exception (Timeout, NoSuchElement,
     * StaleElement, etc.) is captured into the Actual column instead of skipping
     * the report row. The exception is re-thrown after logging so the test still
     * fails — but the report now always shows the Expected vs Actual table.
     */
    public static void step(String stepDescription, String expected, ThrowingSupplier<String> action) {
        String actual;
        try {
            actual = action.get();
        } catch (Throwable t) {
            String msg = t.getClass().getSimpleName() + ": " +
                    (t.getMessage() == null ? "(no message)" : firstLine(t.getMessage()));
            log(stepDescription, expected, msg, false);
            Assert.fail("Step failed: " + stepDescription +
                    " | expected: " + expected + " | actual: " + msg, t);
            return; // unreachable
        }
        check(stepDescription, expected, actual);
    }

    /** Variant for boolean producers — same exception capture, boolean assertion. */
    public static void stepBool(String stepDescription, String expected, ThrowingBooleanSupplier action) {
        boolean value;
        try {
            value = action.get();
        } catch (Throwable t) {
            String msg = t.getClass().getSimpleName() + ": " +
                    (t.getMessage() == null ? "(no message)" : firstLine(t.getMessage()));
            log(stepDescription, expected, msg, false);
            Assert.fail("Step failed: " + stepDescription +
                    " | expected: " + expected + " | actual: " + msg, t);
            return;
        }
        check(stepDescription, expected, value);
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

    /** Mark the current test as skipped with an Expected/Actual row explaining why. */
    public static void skip(String stepDescription, String reason) {
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) t.skip(buildRow(stepDescription, "skipped", reason, false, true));
        throw new org.testng.SkipException(stepDescription + " - " + reason);
    }

    // ── comparison engine ──

    static boolean matches(String expected, String actual) {
        String e = stripUrlAndNormalize(expected);
        String a = stripUrlAndNormalize(actual);
        if (e.isEmpty()) return a.isEmpty();
        if (a.isEmpty()) return false;

        // Exact normalized match wins outright.
        if (a.equals(e)) return true;

        // Placeholder support: <id>, <name>, etc. → match any non-empty token.
        if (PLACEHOLDER.matcher(e).find()) {
            StringBuilder regex = new StringBuilder(".*");
            Matcher m = PLACEHOLDER.matcher(e);
            int last = 0;
            while (m.find()) {
                regex.append(Pattern.quote(e.substring(last, m.start())));
                regex.append(".+?");
                last = m.end();
            }
            regex.append(Pattern.quote(e.substring(last)));
            regex.append(".*");
            if (a.matches(regex.toString())) return true;
        }

        // Fallback: bidirectional case-insensitive substring after URL normalisation.
        return a.contains(e) || e.contains(a);
    }

    static String stripUrlAndNormalize(String s) {
        if (s == null) return "";
        return URL_PREFIX.matcher(s).replaceAll("").trim().toLowerCase();
    }

    // ── internal rendering ──

    private static void log(String stepDescription, String expected, String actual, boolean pass) {
        ExtentTest t = ExtentReportManager.getTest();
        if (t == null) return;
        String row = buildRow(stepDescription, expected, actual, pass, false);
        if (pass) t.pass(row); else t.fail(row);
    }

    private static String buildRow(String stepDescription, String expected, String actual,
                                   boolean pass, boolean skipped) {
        String label;
        String color;
        if (skipped) { label = "SKIP"; color = "#f59e0b"; }
        else if (pass) { label = "PASS"; color = "#22c55e"; }
        else { label = "FAIL"; color = "#ef4444"; }

        return "<table style='border-collapse:collapse;width:100%;font-size:13px;margin:2px 0;'>"
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
              +     label + "</td>"
              + "</tr></table>";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String firstLine(String s) {
        int nl = s.indexOf('\n');
        return nl < 0 ? s : s.substring(0, nl);
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T> {
        T get() throws Exception;
    }

    @FunctionalInterface
    public interface ThrowingBooleanSupplier {
        boolean get() throws Exception;
    }

    private StepReporter() {}
}
