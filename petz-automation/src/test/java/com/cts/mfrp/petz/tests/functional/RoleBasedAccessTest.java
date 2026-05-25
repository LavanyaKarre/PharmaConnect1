package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.models.testdata.RoleAccessCase;
import com.cts.mfrp.petz.models.testdata.RoleAccessCases;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.SidebarComponent;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.BASE_URL;
import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;

/**
 * Role-Based Access scenario — PETZ_TC080 to PETZ_TC084.
 * Group: roleAccess + functional (all five exercise router-guard behaviour, not layout).
 *
 * <p><b>Test data source:</b> {@code src/test/resources/testdata/role-access.xml}.
 * Each @Test method below fetches its own row by id via {@link RoleAccessCases#byId(String)}.
 * The XML carries only the per-case inputs/expectations (which role logs in, which
 * routes are blocked, what URL fragment the app redirects to, and the optional
 * sidebar role assertion). Runtime config — BASE_URL and seed credentials —
 * stays in {@link com.cts.mfrp.petz.constants.AppConstants}, so:
 *   - XML stores RELATIVE paths only (e.g. {@code /ngo/animals}); the test composes
 *     full URLs at runtime via {@link com.cts.mfrp.petz.constants.AppConstants#BASE_URL}.
 *   - XML refers to roles by symbolic key (PET_OWNER / NGO / HOSPITAL / NONE);
 *     the test dispatches to the right LoginPage helper — credentials never
 *     appear in the fixture.
 *
 * <p>Refactor preserved: method names, count, @Test groups/descriptions/priority,
 * the redirect-URL assertion shape, and the additional sidebar role-label check
 * on TC081. No new test cases were introduced.
 *
 * The guard contract this scenario verifies:
 *   - Anonymous user hitting any guarded route -> /auth/login
 *   - Pet Owner hitting NGO/Hospital routes    -> /dashboard
 *   - NGO hitting Hospital routes              -> /ngo (spec) / /dashboard (live)
 *   - Hospital hitting NGO routes              -> /hospital (spec) / /dashboard (live)
 */
public class RoleBasedAccessTest extends BaseTest {

    /** Resolve the symbolic loginAs key against AppConstants-backed LoginPage helpers. */
    private void performLogin(String loginAs) {
        if (loginAs == null || "NONE".equalsIgnoreCase(loginAs)) return;
        LoginPage login = new LoginPage(driver);
        switch (loginAs.toUpperCase()) {
            case "PET_OWNER" -> login.loginAsPetOwner();
            case "NGO"       -> login.loginAsNgo();
            case "HOSPITAL"  -> login.loginAsHospital();
            default -> throw new IllegalArgumentException(
                    "Unknown loginAs key in role-access.xml: " + loginAs);
        }
    }

    private void assertRedirectsTo(String targetUrl, String expectedUrlPart) {
        driver.get(targetUrl);
        new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT))
                .until(ExpectedConditions.urlContains(expectedUrlPart));
        Assert.assertTrue(driver.getCurrentUrl().contains(expectedUrlPart),
                "Expected redirect from " + targetUrl + " to a URL containing '"
                        + expectedUrlPart + "', but was: " + driver.getCurrentUrl());
    }

    /** Run every blocked route in the case through assertRedirectsTo. */
    private void runCase(RoleAccessCase tc) {
        performLogin(tc.getLoginAs());
        for (String route : tc.getBlockedRoutes()) {
            assertRedirectsTo(BASE_URL + route, tc.getExpectedRedirectFragment());
        }
        if (tc.getExpectedSidebarRole() != null && !tc.getExpectedSidebarRole().isBlank()) {
            SidebarComponent sidebar = new SidebarComponent(driver);
            Assert.assertEquals(sidebar.getRoleLabel(), tc.getExpectedSidebarRole(),
                    "After cross-role redirect, sidebar role label should remain '"
                            + tc.getExpectedSidebarRole() + "' for the logged-in user.");
        }
    }

    @Test(priority = 80,
          groups = {"roleAccess", "functional", "regression", "sanity", "negative"},
          description = "PETZ_TC080 - Anonymous user is sent to /auth/login on every guarded route")
    public void TC080_GuardRedirectAnonymous() {
        runCase(RoleAccessCases.byId("TC080"));
    }

    @Test(priority = 81,
          groups = {"roleAccess", "functional", "regression", "sanity", "negative"},
          description = "PETZ_TC081 - Pet Owner cannot enter NGO routes (redirect to /dashboard)")
    public void TC081_PetOwnerCannotEnterNGO() {
        runCase(RoleAccessCases.byId("TC081"));
    }

    @Test(priority = 82,
          groups = {"roleAccess", "functional", "regression", "sanity", "negative"},
          description = "PETZ_TC082 - Pet Owner cannot enter Hospital routes (redirect to /dashboard)")
    public void TC082_PetOwnerCannotEnterHospital() {
        runCase(RoleAccessCases.byId("TC082"));
    }

    @Test(priority = 83,
          groups = {"roleAccess", "functional", "regression", "sanity", "negative"},
          description = "PETZ_TC083 - NGO user cannot enter Hospital routes (redirect to a safe page)")
    public void TC083_NGOCannotEnterHospital() {
        runCase(RoleAccessCases.byId("TC083"));
    }

    @Test(priority = 84,
          groups = {"roleAccess", "functional", "regression", "sanity", "negative"},
          description = "PETZ_TC084 - Hospital user cannot enter NGO routes (redirect to a safe page)")
    public void TC084_HospitalCannotEnterNGO() {
        runCase(RoleAccessCases.byId("TC084"));
    }
}
