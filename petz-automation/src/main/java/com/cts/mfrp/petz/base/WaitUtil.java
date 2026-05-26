package com.cts.mfrp.petz.base;

import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Thin wrapper around WebDriverWait so step defs and Page Objects can ask for waits
 * without re-creating the wait object every time.
 */
public class WaitUtil {

    public static WebDriverWait wait(WebDriver driver) {
        return new WebDriverWait(driver, Duration.ofSeconds(AppConstants.EXPLICIT_WAIT));
    }

    public static WebDriverWait wait(WebDriver driver, int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    public static WebElement waitVisible(WebDriver driver, By locator) {
        return wait(driver).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public static WebElement waitClickable(WebDriver driver, By locator) {
        return wait(driver).until(ExpectedConditions.elementToBeClickable(locator));
    }

    public static boolean waitUrlContains(WebDriver driver, String fragment) {
        return wait(driver).until(ExpectedConditions.urlContains(fragment));
    }

    public static boolean waitUrlContains(WebDriver driver, String fragment, int seconds) {
        return wait(driver, seconds).until(ExpectedConditions.urlContains(fragment));
    }

    private WaitUtil() {}
}
