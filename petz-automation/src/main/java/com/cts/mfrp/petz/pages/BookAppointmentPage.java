package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * Book Appointment at /appointments/book.
 */
public class BookAppointmentPage extends BasePage {

    private static final By DATE_INPUT   = By.cssSelector("input[type='date'], input[formcontrolname='date']");
    private static final By TIME_SELECT  = By.cssSelector("select[formcontrolname='time'], mat-select[formcontrolname='time']");
    private static final By REASON_INPUT = By.cssSelector("textarea[formcontrolname='reason'], textarea[name='reason']");
    private static final By CONFIRM_BTN  = By.xpath("//button[contains(normalize-space(),'Confirm') or contains(normalize-space(),'Book')]");

    public BookAppointmentPage open() {
        goTo(AppConstants.APPOINTMENTS_BOOK_URL);
        return this;
    }

    public boolean isDateVisible()   { return isPresent(DATE_INPUT); }
    public boolean isReasonVisible() { return isPresent(REASON_INPUT); }

    public void fillDate(String yyyymmdd) { type(DATE_INPUT, yyyymmdd); }
    public void fillReason(String v)      { type(REASON_INPUT, v); }

    public boolean isConfirmDisabled() {
        String d = attributeOf(CONFIRM_BTN, "disabled");
        return d != null && !"false".equals(d);
    }

    public void clickConfirm() { click(CONFIRM_BTN); }
}
