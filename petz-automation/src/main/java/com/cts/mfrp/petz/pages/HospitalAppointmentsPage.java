package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * Hospital appointments at /hospital/appointments.
 */
public class HospitalAppointmentsPage extends BasePage {

    private static final By PAGE_TITLE     = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(),'Appointment')]");
    private static final By TABS           = By.cssSelector(".mat-tab-label, [role='tab'], .tab");
    private static final By SEARCH_INPUT   = By.cssSelector("input[placeholder*='Search'], input[name='search']");
    private static final By APPT_ROWS      = By.cssSelector(".appointment-row, mat-card, tr");
    private static final By CONFIRM_BTN    = By.xpath("(//button[contains(normalize-space(),'Confirm')])[1]");
    private static final By COMPLETE_BTN   = By.xpath("(//button[contains(normalize-space(),'Mark Complete') or contains(normalize-space(),'Complete')])[1]");

    public HospitalAppointmentsPage open() {
        goTo(AppConstants.HOSPITAL_APPOINTMENTS_URL);
        return this;
    }

    public boolean isTitleVisible()  { return isPresent(PAGE_TITLE); }
    public int     getTabCount()     { return findAll(TABS).size(); }
    public boolean hasSearchInput()  { return isPresent(SEARCH_INPUT); }
    public int     getApptCount()    { return findAll(APPT_ROWS).size(); }

    public void clickTab(String label) {
        click(By.xpath("//*[(@role='tab' or contains(@class,'tab'))][contains(normalize-space(),'" + label + "')]"));
    }

    public void search(String text) { type(SEARCH_INPUT, text); }
    public void confirmFirst()      { click(CONFIRM_BTN); }
    public void completeFirst()     { click(COMPLETE_BTN); }
}
