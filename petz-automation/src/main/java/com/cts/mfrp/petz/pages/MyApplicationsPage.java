package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * My Applications at /adoption/my.
 */
public class MyApplicationsPage extends BasePage {

    private static final By PAGE_TITLE   = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(),'My Applications') or contains(normalize-space(),'Applications')]");
    private static final By APP_CARDS    = By.cssSelector(".application-card, .app-card, mat-card");
    private static final By STATUS_BADGE = By.cssSelector(".status, .badge, [class*='status']");
    private static final By EMPTY_STATE  = By.xpath("//*[contains(normalize-space(),'No applications')]");
    private static final By BROWSE_CTA   = By.xpath("//*[self::a or self::button][contains(normalize-space(),'Browse Animals') or contains(normalize-space(),'Browse')]");

    public MyApplicationsPage open() {
        goTo(AppConstants.ADOPTION_MY_URL);
        return this;
    }

    public boolean isTitleVisible()      { return isPresent(PAGE_TITLE); }
    public int     getCardCount()        { return findAll(APP_CARDS).size(); }
    public boolean isEmptyStateVisible() { return isPresent(EMPTY_STATE); }
    public boolean hasStatusBadge()      { return isPresent(STATUS_BADGE); }
    public boolean isBrowseCtaVisible()  { return isPresent(BROWSE_CTA); }
}
