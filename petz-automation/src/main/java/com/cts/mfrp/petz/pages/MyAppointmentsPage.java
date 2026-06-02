package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * My Appointments at /appointments.
 */
public class MyAppointmentsPage extends BasePage {

    private static final By PAGE_TITLE     = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(),'My Appointments') or contains(normalize-space(),'Appointments')]");
    private static final By APPT_ROWS      = By.cssSelector(".appointment-card, .appt, mat-card");
    private static final By STATUS_BADGE   = By.cssSelector(".status, .badge, [class*='status']");
    private static final By EMPTY_STATE    = By.xpath("//*[contains(normalize-space(),'No appointments') or contains(normalize-space(),'No bookings')]");
    private static final By BOOK_NOW_CTA   = By.xpath("//*[self::a or self::button][contains(normalize-space(),'Book Now') or contains(normalize-space(),'Book')]");

    public MyAppointmentsPage open() {
        goTo(AppConstants.APPOINTMENTS_URL);
        return this;
    }

    public boolean isTitleVisible()      { return isPresent(PAGE_TITLE); }
    public int     getApptCount()        { return findAll(APPT_ROWS).size(); }
    public boolean isEmptyStateVisible() { return isPresent(EMPTY_STATE); }
    public boolean hasStatusBadge()      { return isPresent(STATUS_BADGE); }
    public boolean isBookNowVisible()    { return isPresent(BOOK_NOW_CTA); }
}
