package com.cts.mfrp.petz.models.testdata;

import com.cts.mfrp.petz.utils.XmlDataLoader;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

/**
 * Root of {@code src/test/resources/testdata/dashboard-nav.xml}. Carries the
 * Quick Action label/route table, the Sidebar label/route table, and the list
 * of labels TC025 asserts are present in the sidebar.
 *
 * <p>Parsed once and cached — call {@link #load()} from a test method.
 */
@JacksonXmlRootElement(localName = "dashboardNav")
public class DashboardNav {

    private static final String RESOURCE = "testdata/dashboard-nav.xml";
    private static volatile DashboardNav cached;

    @JacksonXmlElementWrapper(localName = "quickActions")
    @JacksonXmlProperty(localName = "action")
    private List<NavItem> quickActions;

    @JacksonXmlElementWrapper(localName = "sidebarItems")
    @JacksonXmlProperty(localName = "sidebarItem")
    private List<NavItem> sidebarItems;

    @JacksonXmlElementWrapper(localName = "sidebarExpectedLabels")
    @JacksonXmlProperty(localName = "label")
    private List<String> sidebarExpectedLabels;

    public List<NavItem> getQuickActions()                 { return quickActions; }
    public void setQuickActions(List<NavItem> value)       { this.quickActions = value; }

    public List<NavItem> getSidebarItems()                 { return sidebarItems; }
    public void setSidebarItems(List<NavItem> value)       { this.sidebarItems = value; }

    public List<String> getSidebarExpectedLabels()         { return sidebarExpectedLabels; }
    public void setSidebarExpectedLabels(List<String> v)   { this.sidebarExpectedLabels = v; }

    public static DashboardNav load() {
        if (cached == null) {
            synchronized (DashboardNav.class) {
                if (cached == null) {
                    cached = XmlDataLoader.load(RESOURCE, DashboardNav.class);
                }
            }
        }
        return cached;
    }
}
