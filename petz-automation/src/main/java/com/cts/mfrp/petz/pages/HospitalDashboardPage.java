package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import org.openqa.selenium.By;

/**
 * Hospital dashboard at /hospital.
 */
public class HospitalDashboardPage extends BasePage {

    private static final By PAGE_TITLE    = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(),'Hospital') or contains(normalize-space(),'Dashboard')]");
    private static final By STAT_TILES    = By.cssSelector(".stat-tile, .kpi, .stats-card, [class*='stat']");
    private static final By INFO_CARDS    = By.cssSelector(".info-card, [class*='info-card']");
    private static final By NAV_CARDS     = By.cssSelector(".nav-card, [class*='nav-card']");
    private static final By WEEK_RANGE    = By.xpath("//*[contains(normalize-space(),'Week') or contains(@class,'week')]");

    public boolean isTitleVisible()    { return isPresent(PAGE_TITLE); }
    public int     getStatTileCount()  { return findAll(STAT_TILES).size(); }
    public int     getInfoCardCount()  { return findAll(INFO_CARDS).size(); }
    public int     getNavCardCount()   { return findAll(NAV_CARDS).size(); }
    public boolean isWeekRangeVisible(){ return isPresent(WEEK_RANGE); }
}
