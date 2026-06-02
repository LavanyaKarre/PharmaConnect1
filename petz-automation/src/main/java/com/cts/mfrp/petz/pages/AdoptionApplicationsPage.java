package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * NGO Adoption Applications review at /ngo/applications.
 */
public class AdoptionApplicationsPage extends BasePage {

    private static final By PAGE_TITLE   = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(),'Application') or contains(normalize-space(),'Adoption')]");
    private static final By APP_CARDS    = By.cssSelector(".application-card, mat-card");
    private static final By SEARCH_INPUT = By.cssSelector("input[name='search'], input[placeholder*='Search']");
    private static final By STATUS_SELECT= By.cssSelector("select[formcontrolname='status'], mat-select[formcontrolname='status']");
    private static final By APPROVE_BTN  = By.xpath("(//button[contains(normalize-space(),'Approve')])[1]");
    private static final By REJECT_BTN   = By.xpath("(//button[contains(normalize-space(),'Reject')])[1]");
    private static final By NOTES_INPUT  = By.cssSelector("textarea[formcontrolname='notes'], textarea[name='notes']");
    private static final By CONFIRM_BTN  = By.xpath("//button[contains(normalize-space(),'Confirm') or contains(normalize-space(),'Submit') or contains(normalize-space(),'Save')]");
    private static final By EMPTY_STATE  = By.xpath("//*[contains(normalize-space(),'No applications')]");

    public AdoptionApplicationsPage open() {
        goTo(AppConstants.NGO_APPLICATIONS_URL);
        return this;
    }

    public boolean isTitleVisible()       { return isPresent(PAGE_TITLE); }
    public int     getCardCount()         { return findAll(APP_CARDS).size(); }
    public boolean isSearchVisible()      { return isPresent(SEARCH_INPUT); }
    public boolean isStatusFilterVisible(){ return isPresent(STATUS_SELECT); }
    public boolean isEmptyStateVisible()  { return isPresent(EMPTY_STATE); }

    public void clickApproveFirst() { click(APPROVE_BTN); }
    public void clickRejectFirst()  { click(REJECT_BTN); }
    public void fillNotes(String v) { type(NOTES_INPUT, v); }
    public void clickConfirm()      { click(CONFIRM_BTN); }
}
