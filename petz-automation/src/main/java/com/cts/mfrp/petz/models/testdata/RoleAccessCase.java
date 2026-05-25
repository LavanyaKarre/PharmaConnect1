package com.cts.mfrp.petz.models.testdata;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

/**
 * One row in {@code src/test/resources/testdata/role-access.xml}, consumed by
 * RoleBasedAccessTest. Carries only the per-case input/expectation; runtime
 * config (base URL, seed credentials) is composed at request time from
 * {@link com.cts.mfrp.petz.constants.AppConstants}.
 */
public class RoleAccessCase {

    private String id;
    private String description;
    private String loginAs;                    // NONE | PET_OWNER | NGO | HOSPITAL

    @JacksonXmlElementWrapper(localName = "blockedRoutes")
    @JacksonXmlProperty(localName = "route")
    private List<String> blockedRoutes;

    private String expectedRedirectFragment;
    private String expectedSidebarRole;        // optional

    public String getId()                                { return id; }
    public void   setId(String value)                    { this.id = value; }

    public String getDescription()                       { return description; }
    public void   setDescription(String value)           { this.description = value; }

    public String getLoginAs()                           { return loginAs; }
    public void   setLoginAs(String value)               { this.loginAs = value; }

    public List<String> getBlockedRoutes()               { return blockedRoutes; }
    public void   setBlockedRoutes(List<String> value)   { this.blockedRoutes = value; }

    public String getExpectedRedirectFragment()          { return expectedRedirectFragment; }
    public void   setExpectedRedirectFragment(String v)  { this.expectedRedirectFragment = v; }

    public String getExpectedSidebarRole()               { return expectedSidebarRole; }
    public void   setExpectedSidebarRole(String value)   { this.expectedSidebarRole = value; }
}
