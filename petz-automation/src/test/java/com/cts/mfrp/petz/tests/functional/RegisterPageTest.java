package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.RegisterPage;
import com.cts.mfrp.petz.utils.StepReporter;
import com.cts.mfrp.petz.utils.Waits;
import org.testng.annotations.Test;

import java.util.List;

import static com.cts.mfrp.petz.constants.AppConstants.PET_OWNER_EMAIL;

/**
 * Register (/auth/register) scenario â€” PETZ_TC013 to PETZ_TC020.
 * Group: authRegister.
 *
 * TC016/17/18 actually submit the form; we generate a unique email per run
 * so subsequent runs don't collide. TC019 reuses a known seeded email to
 * trigger the duplicate-email error.
 */
public class RegisterPageTest extends BaseTest {

    private String uniqueEmail(String role) {
        return "petz_" + role + "_" + System.currentTimeMillis() + "@example.com";
    }

    @Test(priority = 13,
          groups = {"authRegister", "ui", "regression", "sanity", "positive"},
          description = "PETZ_TC013 - Validate /auth/register layout")
    public void TC013_RegisterRender() {
        RegisterPage page = new RegisterPage(driver);
        page.open();

        StepReporter.check("Card title",
                "'Create account' heading visible", page.isCardTitleVisible());
        StepReporter.check("Left panel title",
                "'Join the PETZ Community' visible", page.isLeftPanelTitleVisible());
        StepReporter.check("Left panel role bullets",
                "Pet Owner / NGO / Veterinary bullets visible",
                page.leftPanelRoleBulletsVisible());
        StepReporter.check("'Sign in' footer link",
                "Sign in link visible", page.isSignInLinkVisible());
        StepReporter.check("'Create Account' starts disabled",
                "Disabled when form is empty", page.isCreateAccountDisabled());

        StepReporter.note("Full Name placeholder",
                "John Doe", page.getPlaceholder("name"));
        StepReporter.note("Phone placeholder",
                "+91 00000 00000", page.getPlaceholder("phone"));
        StepReporter.note("Email placeholder",
                "you@example.com", page.getPlaceholder("email"));
        StepReporter.note("Password placeholder",
                "min. 6 characters", page.getPlaceholder("password"));
    }

    @Test(priority = 14,
          groups = {"authRegister", "ui", "regression", "positive"},
          description = "PETZ_TC014 - Account Type dropdown lists exactly Pet Owner / NGO / Vet")
    public void TC014_RegisterAccountTypeOptions() {
        RegisterPage page = new RegisterPage(driver);
        page.open();

        List<String> options = page.getAccountTypeOptions();
        String joined = String.join(" | ", options);

        StepReporter.check("Dropdown contains Pet Owner",
                "Pet Owner option present", joined);
        StepReporter.check("Dropdown contains NGO",
                "NGO option present", joined);
        StepReporter.check("Dropdown contains Veterinary",
                "Veterinary option present", joined);
        StepReporter.check("Dropdown has exactly 3 options",
                "3 options (no Admin/Reporter/etc.)", options.size() == 3);
    }

    @Test(priority = 15,
          groups = {"authRegister", "functional", "regression", "negative"},
          description = "PETZ_TC015 - Progressive rules that enable 'Create Account'")
    public void TC015_RegisterButtonDisabledRules() {
        RegisterPage page = new RegisterPage(driver);
        page.open();

        StepReporter.check("Initial state",
                "Create Account is disabled", page.isCreateAccountDisabled());

        page.fillFullName("Some User");
        page.fillPhone("+919876543210");
        page.fillEmail("user@example.com");
        StepReporter.check("N@me + Phone + valid Email only",
                "Create Account still disabled (no password yet)",
                page.isCreateAccountDisabled());

        page.fillPassword("abc");
        StepReporter.check("Password too short (3 chars)",
                "Create Account remains disabled", page.isCreateAccountDisabled());

        page.fillPassword("abc123");
        page.fillConfirmPassword("abc123");
        page.selectAccountType("Pet Owner");
        StepReporter.check("Password >= 6 chars + role chosen",
                "Create Account is enabled (orange)", page.isCreateAccountEnabled());

        page.fillEmail("not-an-email");
        StepReporter.check("After breaking the email syntax",
                "Create Account disables again", page.isCreateAccountDisabled());
    }

    @Test(priority = 16,
          groups = {"authRegister", "functional", "regression", "sanity", "positive"},
          description = "PETZ_TC016 - Successful registration as Pet Owner routes to /dashboard")
    public void TC016_RegisterPetOwnerSuccess() {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        String email = uniqueEmail("po");

        page.fillFullName("Test PetOwner");
        page.fillPhone("+919876543210");
        page.fillEmail(email);
        page.fillPassword("Strong@123");
        page.fillConfirmPassword("Strong@123");
        page.selectAccountType("Pet Owner");

        StepReporter.check("All fields valid",
                "Create Account is enabled", page.isCreateAccountEnabled());

        page.clickCreateAccount();
        Waits.urlContains(driver, "/dashboard");

        StepReporter.check("After submit",
                "/dashboard", page.getCurrentUrl());
    }

    @Test(priority = 17,
          groups = {"authRegister", "functional", "regression", "positive"},
          description = "PETZ_TC017 - NGO registration succeeds and routes to /auth/login with admin-approval toast")
    public void TC017_RegisterNGOSuccess() {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        String email = uniqueEmail("ngo");

        page.fillFullName("Test NGO");
        page.fillPhone("+919876543211");
        page.fillEmail(email);
        page.fillPassword("Strong@123");
        page.fillConfirmPassword("Strong@123");
        page.selectAccountType("NGO");

        page.clickCreateAccount();
        // Per docs/API.md: NGO accounts need admin approval before login. The app
        // routes new NGO registrations to /auth/login with a "pending admin approval"
        // toast instead of /ngo (verified against the live build 2026-05-25).
        Waits.urlContains(driver, "/auth/login");

        StepReporter.check("After submit",
                "/auth/login (admin approval pending)", page.getCurrentUrl());
        StepReporter.check("Admin-approval toast",
                "Toast mentions pending admin approval",
                driver.getPageSource().toLowerCase().contains("admin approval"));
    }

    @Test(priority = 18,
          groups = {"authRegister", "functional", "regression", "positive"},
          description = "PETZ_TC018 - Vet registration succeeds and routes to /auth/login with admin-approval toast")
    public void TC018_RegisterVetSuccess() {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        String email = uniqueEmail("vet");

        page.fillFullName("Test Vet");
        page.fillPhone("+919876543212");
        page.fillEmail(email);
        page.fillPassword("Strong@123");
        page.fillConfirmPassword("Strong@123");
        page.selectAccountType("Veterinary");

        page.clickCreateAccount();
        // Per docs/API.md: HOSPITAL accounts need admin approval before login. Same
        // admin-approval flow as NGO above.
        Waits.urlContains(driver, "/auth/login");

        StepReporter.check("After submit",
                "/auth/login (admin approval pending)", page.getCurrentUrl());
        StepReporter.check("Admin-approval toast",
                "Toast mentions pending admin approval",
                driver.getPageSource().toLowerCase().contains("admin approval"));
    }

    @Test(priority = 19,
          groups = {"authRegister", "functional", "regression", "negative"},
          description = "PETZ_TC019 - Duplicate email is rejected")
    public void TC019_RegisterDuplicateEmail() {
        RegisterPage page = new RegisterPage(driver);
        page.open();

        page.fillFullName("Dup User");
        page.fillPhone("+919876543299");
        page.fillEmail(PET_OWNER_EMAIL);     // an email that's already in the system
        page.fillPassword("Strong@123");
        page.fillConfirmPassword("Strong@123");
        page.selectAccountType("Pet Owner");
        page.clickCreateAccount();
        Waits.documentReady(driver);

        boolean stillOnRegister = page.getCurrentUrl().contains("/auth/register");
        StepReporter.check("URL after duplicate-email submit",
                "Remains on /auth/register", page.getCurrentUrl());
        StepReporter.check("Error surface visible (toast / inline) OR stayed on form",
                "Some error indication present", stillOnRegister || page.hasErrorMessage());
    }

    @Test(priority = 20,
          groups = {"authRegister", "functional", "regression", "sanity", "positive"},
          description = "PETZ_TC020 - 'Sign in' link returns to /auth/login")
    public void TC020_RegisterSignInLink() {
        RegisterPage page = new RegisterPage(driver);
        page.open();
        page.clickSignInLink();
        StepReporter.check("After clicking 'Sign in'",
                "/auth/login", page.getCurrentUrl());
    }
}
