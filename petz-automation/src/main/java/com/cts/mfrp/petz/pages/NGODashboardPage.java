package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import org.openqa.selenium.By;

/**
 * NGO dashboard at /ngo.
 */
public class NGODashboardPage extends BasePage {

    private static final By PAGE_TITLE    = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(),'NGO Dashboard') or contains(normalize-space(),'Dashboard')]");
    // Match the four stat-label texts directly (live NGO dashboard renders them in caps under the big number)
    private static final By STAT_TILES    = By.xpath("//*[contains(normalize-space(),'ANIMALS LISTED') or contains(normalize-space(),'TOTAL RESCUES') or contains(normalize-space(),'APPLICATIONS') or contains(normalize-space(),'COMPLETED RESCUES')]");
    private static final By DONUT_CHARTS  = By.cssSelector(".chart, canvas, [class*='donut'], [class*='chart']");
    // Quick-action links route under /ngo/* (Manage Animals / Rescue Queue / Adoption Applications)
    private static final By QUICK_ACTIONS = By.xpath("//a[contains(@href,'/ngo/')] | //*[contains(normalize-space(),'View Queue') or contains(normalize-space(),'Manage Animals') or contains(normalize-space(),'Adoption Applications')]");
    private static final By SIDEBAR       = By.cssSelector("mat-sidenav, .sidebar, [class*='sidebar']");

    public boolean isTitleVisible()   { return isPresent(PAGE_TITLE); }
    public int     getChartCount()    { return findAll(DONUT_CHARTS).size(); }
    public boolean hasQuickActions()  { return isPresent(QUICK_ACTIONS); }
    public boolean isSidebarVisible() { return isVisible(SIDEBAR); }

    /**
     * Counts NGO stat tiles by searching the rendered page source for the four
     * known label strings. More reliable than CSS selectors because the live
     * NGO dashboard uses CSS text-transform:uppercase on lowercase DOM text,
     * which thwarts XPath text() match.
     */
    public int getStatTileCount() {
        String src = driver.getPageSource().toLowerCase();
        int count = 0;
        if (src.contains("animals listed"))    count++;
        if (src.contains("total rescues"))     count++;
        if (src.contains("applications"))      count++;
        if (src.contains("completed rescues")) count++;
        return count;
    }
}
