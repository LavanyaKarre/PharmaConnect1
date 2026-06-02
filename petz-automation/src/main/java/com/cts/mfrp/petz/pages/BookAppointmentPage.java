package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

/**
 * Book Appointment at /appointments/book.
 *
 * Live form has 3 steps:
 *   Step 1 — pick Hospital (mat-select), then Doctor (mat-select, disabled until hospital picked)
 *   Step 2 — pick Date (mat-datepicker, readonly input) and Time slot (mat-select)
 *   Step 3 — Reason (textarea)
 */
public class BookAppointmentPage extends BasePage {

    private static final By HOSPITAL_SELECT = By.cssSelector("mat-select[formcontrolname='hospitalId']");
    private static final By DOCTOR_SELECT   = By.cssSelector("mat-select[formcontrolname='doctorId']");
    private static final By DATE_INPUT      = By.cssSelector("input[formcontrolname='apptDate']");
    private static final By TIME_SELECT     = By.cssSelector("mat-select[formcontrolname='apptTime']");
    private static final By REASON_INPUT    = By.cssSelector("textarea[formcontrolname='reason']");
    private static final By CONFIRM_BTN     = By.xpath("//button[contains(normalize-space(),'Confirm Booking')]");
    private static final By MAT_OPTIONS     = By.cssSelector("mat-option");

    public BookAppointmentPage open() {
        goTo(AppConstants.APPOINTMENTS_BOOK_URL);
        return this;
    }

    /**
     * Open a mat-select and click the first available option.
     *
     * Returns true when an option was found and clicked. Returns false (no exception)
     * when the dropdown remained empty — the caller can decide whether that's fatal.
     * This handles two real failure modes seen on the live site:
     *   1. Headless click on mat-select sometimes focuses but doesn't open the panel;
     *      we retry with a JS click if no options appear quickly.
     *   2. The list may genuinely be empty for this account (e.g. no hospitals seeded
     *      for the test pet owner). Returning false instead of throwing lets the test
     *      continue and report a clear "no data" outcome rather than a 15 s timeout.
     */
    private boolean pickFirstFrom(By matSelect) {
        WebElement el = findClickable(matSelect);
        el.click();
        shortPause();

        if (findAll(MAT_OPTIONS).isEmpty()) {
            // Fallback: JS click sometimes opens panels that WebDriver's click doesn't (headless quirk).
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }

        try {
            new org.openqa.selenium.support.ui.WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.presenceOfElementLocated(MAT_OPTIONS));
        } catch (TimeoutException te) {
            return false;   // dropdown stayed empty - no data
        }

        List<WebElement> options = findAll(MAT_OPTIONS);
        if (options.isEmpty()) return false;
        options.get(0).click();
        shortPause();
        return true;
    }

    /** Pick the first hospital; returns false if dropdown was empty. */
    public boolean selectFirstHospital() { return pickFirstFrom(HOSPITAL_SELECT); }

    /** Pick the first doctor (server fetches after hospital pick); returns false if empty. */
    public boolean selectFirstDoctor()   { return pickFirstFrom(DOCTOR_SELECT); }

    /** Pick the first available time slot; returns false if empty. */
    public boolean selectFirstTime()     { return pickFirstFrom(TIME_SELECT); }


    /**
     * Fill the mat-datepicker. The input is readonly so a plain sendKeys would no-op.
     * Strip readonly via JS, type in MM/DD/YYYY (Angular Material en-US default), TAB to commit.
     */
    public void fillDate(String yyyymmdd) {
        WebElement input = findVisible(DATE_INPUT);
        ((JavascriptExecutor) driver).executeScript("arguments[0].removeAttribute('readonly')", input);
        LocalDate ld = LocalDate.parse(yyyymmdd);
        String formatted = String.format("%02d/%02d/%d", ld.getMonthValue(), ld.getDayOfMonth(), ld.getYear());
        input.clear();
        input.sendKeys(formatted);
        input.sendKeys(Keys.TAB);
        shortPause();
    }

    public void fillReason(String v) { type(REASON_INPUT, v); }

    public boolean isConfirmDisabled() {
        String d = attributeOf(CONFIRM_BTN, "disabled");
        return d != null && !"false".equals(d);
    }

    public void clickConfirm() { click(CONFIRM_BTN); }
}
