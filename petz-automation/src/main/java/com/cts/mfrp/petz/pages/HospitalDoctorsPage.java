package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;
import static com.cts.mfrp.petz.constants.AppConstants.HOSPITAL_DOCTORS_URL;

/**
 * /hospital/doctors. Covers PETZ_TC074 – PETZ_TC079.
 */
public class HospitalDoctorsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'Manage Doctors')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),\"Add and manage your hospital\") and contains(normalize-space(),'medical staff')]");

    private final By addDoctorBtnTop  = By.xpath(
            "(//a[contains(normalize-space(),'Add Doctor') and not(contains(normalize-space(),'First'))]" +
            " | //button[contains(normalize-space(),'Add Doctor') and not(contains(normalize-space(),'First'))])[1]");
    private final By addFirstDoctorBtn = By.xpath(
            "//a[contains(normalize-space(),'Add First Doctor')] | //button[contains(normalize-space(),'Add First Doctor')]");

    private final By emptyStateTitle = By.xpath("//*[contains(normalize-space(),'No doctors added')]");

    // New Doctor form
    private final By formCardTitle    = By.xpath("//*[contains(normalize-space(),'New Doctor Details')]");
    private final By fullNameInput    = By.xpath(
            "//input[@formcontrolname='fullName' or @name='fullName' or contains(@placeholder,'Dr. Jane Smith')]");
    private final By specializationInput = By.xpath(
            "//input[@formcontrolname='specialization' or @name='specialization' or contains(@placeholder,'Surgery')]");
    private final By scheduleStartInput = By.xpath(
            "//input[@formcontrolname='scheduleStart' or @name='scheduleStart' or @formcontrolname='startTime' or contains(@aria-label,'Start')]");
    private final By scheduleEndInput   = By.xpath(
            "//input[@formcontrolname='scheduleEnd' or @name='scheduleEnd' or @formcontrolname='endTime' or contains(@aria-label,'End')]");
    private final By slotDurationInput  = By.xpath(
            "//input[@formcontrolname='slotDuration' or @name='slotDuration' or (@type='number' and ancestor::*[contains(.,'Slot Duration')][1])]");

    // Button text is rendered as "save Save Doctor" — the <mat-icon>save</mat-icon>
    // ligature contributes the leading "save" word to textContent, so exact match fails.
    private final By saveDoctorBtn = By.xpath(
            "//button[contains(normalize-space(),'Save Doctor') or normalize-space()='Save']");
    // Live text is "close Cancel" (mat-icon ligature). The form has *two* Cancel
    // buttons — a top-right ✕ and a footer one — both share the icon-prefix text;
    // contains() matches either and either click discards the form, so we don't
    // disambiguate further.
    private final By cancelBtn = By.xpath("(//button[contains(normalize-space(),'Cancel')])[last()]");

    public HospitalDoctorsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(HOSPITAL_DOCTORS_URL); }

    public boolean isTitleVisible()    { return isVisible(title); }
    public boolean isSubtitleVisible() { return isVisible(subtitle); }
    public boolean isAddDoctorBtnVisible() { return isVisible(addDoctorBtnTop); }

    public boolean areFourStatTilesVisible() {
        String src = driver.getPageSource().toUpperCase();
        return src.contains("TOTAL DOCTORS") && src.contains("SPECIALIZATIONS")
                && src.contains("WITH SCHEDULE") && (src.contains("SLOTS / DAY") || src.contains("SLOTS/DAY") || src.contains("SLOTS PER DAY"));
    }

    public boolean isEmptyStateVisible() { return isVisible(emptyStateTitle); }

    public void clickAddDoctorTop()   { safeClick(addDoctorBtnTop); }
    public void clickAddFirstDoctor() { safeClick(addFirstDoctorBtn); }

    public boolean isAddFormVisible() { return isVisible(formCardTitle) || isVisible(fullNameInput); }

    // Form fields
    public void fillFullName(String v)       { type(fullNameInput, v); }
    public void fillSpecialization(String v) { type(specializationInput, v); }

    public void fillScheduleStart(String hhmm) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(scheduleStartInput));
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        el.sendKeys(hhmm);
    }

    public void fillScheduleEnd(String hhmm) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(scheduleEndInput));
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        el.sendKeys(hhmm);
    }

    public String getSlotDurationValue() {
        try { return driver.findElement(slotDurationInput).getAttribute("value"); }
        catch (Exception e) { return ""; }
    }

    public void setSlotDuration(int minutes) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(slotDurationInput));
        el.click();
        el.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.DELETE);
        el.sendKeys(String.valueOf(minutes));
    }

    public boolean isSaveDoctorDisabled() {
        try {
            WebElement btn = driver.findElement(saveDoctorBtn);
            String d = btn.getAttribute("disabled");
            String a = btn.getAttribute("aria-disabled");
            return (d != null && !d.equals("false")) || "true".equalsIgnoreCase(a);
        } catch (Exception e) { return true; }
    }

    public void clickSaveDoctor() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(saveDoctorBtn));
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public void clickCancel() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(cancelBtn));
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public int getDoctorRowCount() {
        return driver.findElements(By.cssSelector(
                ".doctor-card, .doctor-row, app-doctor-card, [class*='doctor-card'], [class*='doctor-row']")).size();
    }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    // ── helpers ──
    private void type(By by, String value) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(by));
        el.clear(); el.sendKeys(value);
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
