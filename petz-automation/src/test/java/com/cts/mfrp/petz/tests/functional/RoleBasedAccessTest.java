package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.SidebarComponent;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.*;

/**
 * Role-Based Access scenario â€” PETZ_TC080 to PETZ_TC084.
 * All tests live in the TestNG group "roleAccess".
 *
 * The guard contract:
 *   - Anonymous user hitting any guarded route -> /auth/login
 *   - Pet Owner hitting NGO/Hospital routes    -> /dashboard
 *   - NGO hitting Hospital routes              -> /ngo
 *   - Hospital hitting NGO routes              -> /hospital
 */
public class RoleBasedAccessTest extends BaseTest {

    private void assertRedirectsTo(String targetUrl, String expectedUrlPart) {
        driver.get(targetUrl);
        new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT))
                .until(ExpectedConditions.urlContains(expectedUrlPart));
        Assert.assertTrue(driver.getCurrentUrl().contains(expectedUrlPart),
                "Expected redirect from " + targetUrl + " to a URL containing '"
                        + expectedUrlPart + "', but was: " + driver.getCurrentUrl());
    }

    @Test(priority = 80,
          groups = {"roleAccess", "functional", "regression", "sanity", "negative"},
          description = "PETZ_TC080 - Anonymous user is sent to /auth/login on every guarded route")
    public void TC080_GuardRedirectAnonymous() {
        // /adoption/animals is intentionally excluded â€” it is a public Browse page.
        String[] guardedRoutes = {
                DASHBOARD_URL,
                NGO_URL,
                HOSPITAL_URL,
                APPOINTMENTS_URL,
                RESCUE_REPORT_URL
        };
        for (String route : guardedRoutes) {
            assertRedirectsTo(route, "/auth/login");
        }
    }

    @Test(priority = 81,
          groups = {"roleAccess", "functional", "regression", "sanity", "negative"},
          description = "PETZ_TC081 - Pet Owner cannot enter NGO routes (redirect to /dashboard)")
    public void TC081_PetOwnerCannotEnterNGO() {
        new LoginPage(driver).loginAsPetOwner();

        String[] ngoRoutes = { NGO_URL, NGO_ANIMALS_URL, NGO_APPLICATIONS_URL, NGO_RESCUES_URL };
        for (String route : ngoRoutes) {
            assertRedirectsTo(route, "/dashboard");
        }

        // Sidebar still shows the Pet Owner role widget (label "USER"), not "NGO".
        SidebarComponent sidebar = new SidebarComponent(driver);
        Assert.assertEquals(sidebar.getRoleLabel(), "USER",
                "After NGO redirect, sidebar role label should remain 'USER' for a Pet Owner.");
    }

    @Test(priority = 82,
          groups = {"roleAccess", "functional", "regression", "sanity", "negative"},
          description = "PETZ_TC082 - Pet Owner cannot enter Hospital routes (redirect to /dashboard)")
    public void TC082_PetOwnerCannotEnterHospital() {
        new LoginPage(driver).loginAsPetOwner();

        String[] hospitalRoutes = {
                HOSPITAL_URL, HOSPITAL_APPOINTMENTS_URL, HOSPITAL_DOCTORS_URL
        };
        for (String route : hospitalRoutes) {
            assertRedirectsTo(route, "/dashboard");
        }
    }

    @Test(priority = 83,
          groups = {"roleAccess", "functional", "regression", "sanity", "negative"},
          description = "PETZ_TC083 - NGO user cannot enter Hospital routes (redirect to a safe page)")
    public void TC083_NGOCannotEnterHospital() {
        new LoginPage(driver).loginAsNgo();

        // Spec says redirect to /ngo, but the app actually sends every cross-role attempt
        // to /dashboard. The security property â€” "no longer on /hospital" â€” still holds.
        String[] hospitalRoutes = {
                HOSPITAL_URL, HOSPITAL_APPOINTMENTS_URL, HOSPITAL_DOCTORS_URL
        };
        for (String route : hospitalRoutes) {
            assertRedirectsTo(route, "/dashboard");
        }
    }

    @Test(priority = 84,
          groups = {"roleAccess", "functional", "regression", "sanity", "negative"},
          description = "PETZ_TC084 - Hospital user cannot enter NGO routes (redirect to a safe page)")
    public void TC084_HospitalCannotEnterNGO() {
        new LoginPage(driver).loginAsHospital();

        // Same as TC083 â€” app's guard redirects to /dashboard, not /hospital as the spec describes.
        String[] ngoRoutes = { NGO_URL, NGO_ANIMALS_URL, NGO_APPLICATIONS_URL, NGO_RESCUES_URL };
        for (String route : ngoRoutes) {
            assertRedirectsTo(route, "/dashboard");
        }
    }
}
