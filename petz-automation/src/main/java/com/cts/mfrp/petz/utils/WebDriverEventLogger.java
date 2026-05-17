package com.cts.mfrp.petz.utils;

import com.aventstack.extentreports.ExtentTest;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverListener;

/**
 * Auto-logs every WebDriver action into the current Extent test as an info step.
 * Wired up in DriverFactory via EventFiringDecorator. Tests don't call into this.
 */
public class WebDriverEventLogger implements WebDriverListener {

    private void log(String message) {
        ExtentTest t = ExtentReportManager.getTest();
        if (t != null) t.info(message);
    }

    @Override
    public void beforeGet(WebDriver driver, String url) {
        log("Navigate to: " + url);
    }

    @Override
    public void beforeClick(WebElement element) {
        log("Click: " + describe(element));
    }

    @Override
    public void beforeSendKeys(WebElement element, CharSequence... keysToSend) {
        log("Type into " + describe(element) + ": " + maskIfSensitive(element, keysToSend));
    }

    @Override
    public void beforeFindElement(WebDriver driver, By locator) {
        log("Find element: " + locator);
    }

    @Override
    public void beforeQuit(WebDriver driver) {
        log("Quit browser");
    }

    private String describe(WebElement element) {
        try {
            String tag  = element.getTagName();
            String text = element.getText();
            if (text != null && !text.isBlank()) {
                return "<" + tag + "> '" + text.strip() + "'";
            }
            return "<" + tag + ">";
        } catch (Exception e) {
            return "<stale-or-detached-element>";
        }
    }

    private String maskIfSensitive(WebElement element, CharSequence... keys) {
        try {
            String type = element.getAttribute("type");
            if ("password".equalsIgnoreCase(type)) return "********";
        } catch (Exception ignored) { }
        return String.join("", keys);
    }
}
