package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import org.openqa.selenium.By;

/**
 * Pet Owner dashboard at /dashboard.
 */
public class UserDashboardPage extends BasePage {

    private static final By GREETING       = By.xpath("//*[contains(text(),'Good Morning') or contains(text(),'Good Afternoon') or contains(text(),'Good Evening')]");
    private static final By EMERGENCY_BAN  = By.xpath("//*[contains(@class,'emergency') or contains(.,'Emergency')]");
    private static final By REPORT_NOW     = By.xpath("//*[self::a or self::button][contains(normalize-space(),'Report Now')]");
    private static final By KPI_TILES      = By.cssSelector(".stat-tile, .kpi, .stats-card, [class*='stat-card']");
    private static final By SIDEBAR        = By.cssSelector("mat-sidenav, .sidebar, [class*='sidebar']");
    private static final By BELL_ICON      = By.cssSelector("[class*='bell'], [aria-label='notifications'], mat-icon[fontIcon='notifications']");

    public boolean isGreetingVisible()       { return isPresent(GREETING); }
    public boolean isEmergencyBannerVisible(){ return isPresent(EMERGENCY_BAN); }
    public boolean isReportNowVisible()      { return isPresent(REPORT_NOW); }
    public int     getKpiTileCount()         { return findAll(KPI_TILES).size(); }
    public boolean isSidebarVisible()        { return isVisible(SIDEBAR); }
    public boolean isBellVisible()           { return isPresent(BELL_ICON); }

    public String getGreetingText() {
        return isPresent(GREETING) ? textOf(GREETING) : "";
    }
}
