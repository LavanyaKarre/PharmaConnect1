package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * NGO Rescue Queue at /ngo/rescues.
 */
public class RescueQueuePage extends BasePage {

    private static final By PAGE_TITLE     = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(),'Rescue Queue') or contains(normalize-space(),'Rescue')]");
    private static final By RESCUE_CARDS   = By.cssSelector(".rescue-card, mat-card");
    private static final By ACCEPT_BTN     = By.xpath("(//button[contains(normalize-space(),'Accept')])[1]");
    private static final By DECLINE_BTN    = By.xpath("(//button[contains(normalize-space(),'Decline') or contains(normalize-space(),'Reject')])[1]");
    private static final By STATUS_SELECT  = By.cssSelector("select[formcontrolname='status'], mat-select[formcontrolname='status']");
    private static final By NOTES_INPUT    = By.cssSelector("textarea[formcontrolname='notes'], textarea[name='notes']");
    private static final By UPDATE_BTN     = By.xpath("//button[contains(normalize-space(),'Update') or contains(normalize-space(),'Save')]");
    private static final By EMPTY_STATE    = By.xpath("//*[contains(normalize-space(),'Queue is clear') or contains(normalize-space(),'No rescues')]");

    public RescueQueuePage open() {
        goTo(AppConstants.NGO_RESCUES_URL);
        return this;
    }

    public boolean isTitleVisible()       { return isPresent(PAGE_TITLE); }
    public int     getRescueCount()       { return findAll(RESCUE_CARDS).size(); }
    public boolean isEmptyStateVisible()  { return isPresent(EMPTY_STATE); }

    public void clickAcceptFirst()  { click(ACCEPT_BTN); }
    public void clickDeclineFirst() { click(DECLINE_BTN); }

    public void fillNotes(String v) { type(NOTES_INPUT, v); }
    public void clickUpdate()       { click(UPDATE_BTN); }

    public boolean hasStatusSelector() { return isPresent(STATUS_SELECT); }
    public boolean hasNotesInput()     { return isPresent(NOTES_INPUT); }
}
