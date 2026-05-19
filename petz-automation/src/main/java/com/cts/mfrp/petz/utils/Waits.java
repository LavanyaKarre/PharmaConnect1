package com.cts.mfrp.petz.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.function.Function;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * Reusable explicit-wait helpers. Tests should call these instead of
 * Thread.sleep so we wait for an actual state change (URL, element,
 * page-source) with the shortest possible delay and fail fast when the
 * state never arrives.
 *
 * <p>All helpers honour the project-wide {@code EXPLICIT_WAIT} timeout
 * defined in {@link com.cts.mfrp.petz.constants.AppConstants}.
 */
public final class Waits {

    private Waits() {}

    public static WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public static WebDriverWait wait(WebDriver driver, int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    /** Wait until the current URL contains the given substring. */
    public static void urlContains(WebDriver driver, String fragment) {
        wait(driver).until(ExpectedConditions.urlContains(fragment));
    }

    /** Wait until the current URL no longer contains the given substring. */
    public static void urlNotContaining(WebDriver driver, String fragment) {
        wait(driver).until((Function<WebDriver, Boolean>) d ->
                d.getCurrentUrl() != null && !d.getCurrentUrl().contains(fragment));
    }

    /** Wait until the current URL matches the given regex. */
    public static void urlMatches(WebDriver driver, String regex) {
        wait(driver).until(ExpectedConditions.urlMatches(regex));
    }

    /** Wait until an element matching the locator is visible on the page. */
    public static void visible(WebDriver driver, By locator) {
        wait(driver).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    /** Wait until the page source contains the given (case-insensitive) text. */
    public static void pageSourceContains(WebDriver driver, String text) {
        String needle = text.toLowerCase();
        wait(driver).until((Function<WebDriver, Boolean>) d ->
                d.getPageSource().toLowerCase().contains(needle));
    }

    /** Wait until any of the given case-insensitive needles appears in the page source. */
    public static void pageSourceContainsAny(WebDriver driver, String... needles) {
        wait(driver).until((Function<WebDriver, Boolean>) d -> {
            String src = d.getPageSource().toLowerCase();
            for (String n : needles) if (src.contains(n.toLowerCase())) return true;
            return false;
        });
    }

    /**
     * Wait until the document.readyState is "complete". Useful after
     * router-driven navigations where the URL changes but rendering of the
     * destination view may still be in flight.
     */
    public static void documentReady(WebDriver driver) {
        wait(driver).until((Function<WebDriver, Boolean>) d -> {
            Object state = ((org.openqa.selenium.JavascriptExecutor) d)
                    .executeScript("return document.readyState;");
            return "complete".equals(state);
        });
    }
}
