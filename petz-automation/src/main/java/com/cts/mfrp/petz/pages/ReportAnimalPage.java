package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;
import static com.cts.mfrp.petz.constants.AppConstants.RESCUE_REPORT_URL;

/**
 * /rescue/report. Covers PETZ_TC049 – PETZ_TC056.
 */
public class ReportAnimalPage {

    public static final String URG_LOW      = "Low";
    public static final String URG_MEDIUM   = "Medium";
    public static final String URG_HIGH     = "High";
    public static final String URG_CRITICAL = "Critical";

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title       = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Report Animal in Need')]");
    private final By emergencyBanner = By.xpath("//*[contains(normalize-space(),'Life-threatening emergency')]");
    private final By backArrowIcon = By.xpath(
            "//mat-icon[normalize-space()='arrow_back']/ancestor::button[1] | //button[.//mat-icon[normalize-space()='arrow_back']]");
    private final By backButton  = By.xpath("//button[normalize-space()='Back'] | //a[normalize-space()='Back']");

    // Form controls
    private final By animalTypeSelect = By.xpath(
            "//mat-select[@formcontrolname='animalType' or @formcontrolname='type' or @name='animalType']" +
            " | //label[contains(normalize-space(),'Type of animal')]/following::mat-select[1]");
    // Live form uses formcontrolname='criticality' (not 'urgency') and pre-selects
    // "🟡 Medium" by default — confirmed against the DOM 2026-05-25.
    private final By urgencySelect = By.xpath(
            "//mat-select[@formcontrolname='criticality' or @formcontrolname='urgency' or @formcontrolname='urgencyLevel' or @name='urgency']" +
            " | //label[contains(normalize-space(),'Urgency')]/following::mat-select[1]");
    private final By areaSelect = By.xpath(
            "//mat-select[@formcontrolname='area' or @formcontrolname='chennaiArea' or @name='area']" +
            " | //label[contains(normalize-space(),'Chennai area') or contains(normalize-space(),'area in Chennai')]/following::mat-select[1]");
    private final By landmarkInput = By.xpath(
            "//input[@formcontrolname='landmark' or @formcontrolname='street' or @name='landmark' or contains(@placeholder,'Landmark')]");
    private final By conditionTextarea = By.xpath(
            "//textarea[@formcontrolname='condition' or @formcontrolname='description' or @name='condition' " +
            "or contains(@placeholder,\"animal's condition\") or contains(@placeholder,'animal’s condition')]");

    // Live form has a required reporter-phone field that isn't optional — without
    // it the submit button stays disabled. Confirmed against the DOM 2026-05-25.
    private final By reporterPhoneInput = By.xpath(
            "//input[@formcontrolname='reporterPhone' or @name='reporterPhone' or @type='tel']");
    private final By gpsButton = By.xpath(
            "//button[contains(normalize-space(),'Use My GPS Location') or contains(normalize-space(),'GPS Location')]");

    // Button text may include a mat-icon ligature prefix (e.g. "send Submit Report"),
    // so match by contains() instead of exact text.
    private final By submitButton = By.xpath(
            "//button[contains(normalize-space(),'Submit Report') or contains(normalize-space(),'Send Report') or contains(normalize-space(),'Submit') or @type='submit']");

    private final By matOptions = By.xpath("//mat-option | //*[contains(@class,'mat-mdc-option')]");

    // Required-error labels
    private final By requiredErrors = By.xpath("//*[normalize-space()='Required' or contains(@class,'error')]");

    public ReportAnimalPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(RESCUE_REPORT_URL); }

    // Layout
    public boolean isTitleVisible()           { return isVisible(title); }
    public boolean isEmergencyBannerVisible() { return isVisible(emergencyBanner); }
    public boolean hasAnimalDetailsSection() {
        String src = driver.getPageSource();
        return src.contains("ANIMAL DETAILS") || src.contains("Animal Details") || src.contains("Type of animal");
    }
    public boolean hasLocationSection() {
        String src = driver.getPageSource();
        return src.contains("LOCATION") || src.contains("Location") || src.contains("Chennai area");
    }
    public boolean hasSituationSection() {
        String src = driver.getPageSource();
        return src.contains("SITUATION DETAILS") || src.contains("Situation Details") || src.contains("Describe the animal");
    }

    // Animal type dropdown
    public List<String> openAnimalTypeOptions() {
        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(animalTypeSelect));
        try { sel.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sel);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(matOptions));
        return driver.findElements(matOptions).stream().map(WebElement::getText).map(String::trim).toList();
    }

    public void selectAnimalType(String hint) {
        openAnimalTypeOptions();
        clickOptionContaining(hint);
    }

    public void blurAnimalType() {
        // Open then close without selecting → triggers Required error on blur.
        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(animalTypeSelect));
        try { sel.click(); } catch (Exception ignored) {}
        try { driver.findElement(By.tagName("body")).sendKeys(Keys.ESCAPE); } catch (Exception ignored) {}
    }

    // Urgency dropdown
    public List<String> openUrgencyOptions() {
        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(urgencySelect));
        try { sel.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sel);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(matOptions));
        return driver.findElements(matOptions).stream().map(WebElement::getText).map(String::trim).toList();
    }

    public void selectUrgency(String hint) {
        openUrgencyOptions();
        clickOptionContaining(hint);
    }

    // Area dropdown
    public List<String> openAreaOptions() {
        WebElement sel = wait.until(ExpectedConditions.elementToBeClickable(areaSelect));
        try { sel.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", sel);
        }
        wait.until(ExpectedConditions.visibilityOfElementLocated(matOptions));
        return driver.findElements(matOptions).stream().map(WebElement::getText).map(String::trim).toList();
    }

    public void selectArea(String hint) {
        openAreaOptions();
        clickOptionContaining(hint);
    }

    // Inputs
    public void typeLandmark(String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(landmarkInput));
        el.clear(); el.sendKeys(text);
    }

    public void blurLandmark() {
        try {
            WebElement el = driver.findElement(landmarkInput);
            el.click();
            ((JavascriptExecutor) driver).executeScript("arguments[0].blur();", el);
        } catch (Exception ignored) {}
    }

    public void typeCondition(String text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(conditionTextarea));
        el.clear(); el.sendKeys(text);
    }

    public void typeReporterPhone(String phone) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(reporterPhoneInput));
        el.clear(); el.sendKeys(phone);
    }

    public void blurCondition() {
        try {
            WebElement el = driver.findElement(conditionTextarea);
            el.click();
            ((JavascriptExecutor) driver).executeScript("arguments[0].blur();", el);
        } catch (Exception ignored) {}
    }

    // GPS
    public void clickGpsButton() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(gpsButton));
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    // Submit / back
    public void clickSubmit() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btn);
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public void clickBack() {
        try { safeClick(backButton); }
        catch (Exception e) { safeClick(backArrowIcon); }
    }

    // Required errors
    public int countRequiredErrors() { return driver.findElements(requiredErrors).size(); }
    public boolean hasAnyRequiredError() { return countRequiredErrors() > 0; }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    // ── helpers ──
    private void clickOptionContaining(String hint) {
        String lc = hint.toLowerCase();
        WebElement opt = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(
                "//mat-option[contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lc + "')]" +
                " | //*[contains(@class,'mat-mdc-option') and contains(translate(.,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'" + lc + "')]")));
        try { opt.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", opt);
        }
    }

    private boolean isVisible(By by) {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    private void safeClick(By by) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(by));
        try { el.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
}
