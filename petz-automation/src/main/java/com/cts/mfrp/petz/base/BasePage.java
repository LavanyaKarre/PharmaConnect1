package com.cts.mfrp.petz.base;

import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.utils.ExtentReportManager;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;

/**
 * Reusable helpers shared by every Page Object. Keeps each Page Object slim
 * by hiding away wait/find/click/type plumbing.
 */
public abstract class BasePage {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    protected BasePage() {
        this.driver = DriverFactory.getDriver();
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(AppConstants.EXPLICIT_WAIT));
    }

    // ---- Navigation ----

    public void goTo(String url) {
        driver.get(url);
        ExtentReportManager.logStep("Navigated to " + url);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }

    public String currentRelativePath() {
        String url = driver.getCurrentUrl();
        if (url == null) return "";
        if (url.startsWith(AppConstants.BASE_URL)) {
            return url.substring(AppConstants.BASE_URL.length());
        }
        return url;
    }

    // ---- Element finding ----

    protected WebElement find(By locator) {
        return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    protected WebElement findVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected WebElement findClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    protected List<WebElement> findAll(By locator) {
        return driver.findElements(locator);
    }

    // ---- Element actions ----

    protected void click(By locator) {
        findClickable(locator).click();
        ExtentReportManager.logStep("Clicked " + describe(locator));
    }

    protected void type(By locator, String value) {
        WebElement el = findVisible(locator);
        el.clear();
        if (value != null) el.sendKeys(value);
        // Mask secrets: never echo what was typed into a password field.
        boolean isPassword = "password".equalsIgnoreCase(el.getAttribute("type"));
        String shown = (value == null) ? "" : (isPassword ? "********" : "'" + value + "'");
        ExtentReportManager.logStep("Entered " + shown + " into " + describe(locator));
    }

    protected void blur(By locator) {
        WebElement el = find(locator);
        new Actions(driver).moveToElement(el).perform();
        // tab off the field
        el.sendKeys(Keys.TAB);
    }

    protected void selectByVisibleText(By locator, String text) {
        new Select(findVisible(locator)).selectByVisibleText(text);
        ExtentReportManager.logStep("Selected '" + text + "' in " + describe(locator));
    }

    protected void selectByValue(By locator, String value) {
        new Select(findVisible(locator)).selectByValue(value);
        ExtentReportManager.logStep("Selected value '" + value + "' in " + describe(locator));
    }

    // ---- Element state ----

    public boolean isVisible(By locator) {
        try {
            return findVisible(locator).isDisplayed();
        } catch (TimeoutException | NoSuchElementException e) {
            return false;
        }
    }

    public boolean isPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    public boolean isEnabled(By locator) {
        try {
            return find(locator).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isDisabled(By locator) {
        return !isEnabled(locator);
    }

    public String textOf(By locator) {
        return findVisible(locator).getText();
    }

    public String attributeOf(By locator, String name) {
        return find(locator).getAttribute(name);
    }

    // ---- Convenience ----

    public void shortPause() {
        try { Thread.sleep(500); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }

    public void scrollIntoView(By locator) {
        WebElement el = find(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", el);
    }

    /** Short, readable locator label for report steps (drops the verbose "By." prefix). */
    private static String describe(By locator) {
        return locator.toString().replaceFirst("^By\\.", "");
    }
}
