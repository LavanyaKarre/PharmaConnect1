package com.cts.mfrp.petz.models.testdata;

/**
 * One label/route pair from {@code dashboard-nav.xml}. Used for both Quick
 * Action cards and Sidebar nav items in PetOwnerDashboardTest.
 *
 * <p>{@code routeFragment} is RELATIVE — the test composes it with
 * {@link com.cts.mfrp.petz.constants.AppConstants#BASE_URL} at runtime, so XML
 * never duplicates the base URL.
 */
public class NavItem {

    private String label;
    private String routeFragment;

    public String getLabel()                       { return label; }
    public void   setLabel(String value)           { this.label = value; }

    public String getRouteFragment()               { return routeFragment; }
    public void   setRouteFragment(String value)   { this.routeFragment = value; }
}
