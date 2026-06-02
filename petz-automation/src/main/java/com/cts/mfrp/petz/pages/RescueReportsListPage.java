package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * My Rescue Reports at /rescue.
 */
public class RescueReportsListPage extends BasePage {

    private static final By PAGE_TITLE     = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(),'Rescue') or contains(normalize-space(),'My Reports')]");
    private static final By REPORT_CARDS   = By.cssSelector(".report-card, .rescue-card, mat-card");
    private static final By URGENCY_BADGE  = By.cssSelector(".urgency, [class*='urgency'], .badge");
    private static final By REPORT_BUTTON  = By.xpath("//*[self::a or self::button][contains(normalize-space(),'Report Animal') or contains(normalize-space(),'Report')]");

    public RescueReportsListPage open() {
        goTo(AppConstants.RESCUE_URL);
        return this;
    }

    public boolean isTitleVisible()       { return isPresent(PAGE_TITLE); }
    public int     getReportCount()       { return findAll(REPORT_CARDS).size(); }
    public boolean hasUrgencyBadge()      { return isPresent(URGENCY_BADGE); }
    public boolean isReportButtonVisible(){ return isPresent(REPORT_BUTTON); }
}
