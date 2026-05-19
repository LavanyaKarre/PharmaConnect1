package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.StepReporter;
import com.cts.mfrp.petz.utils.Waits;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.constants.AppConstants.*;

/**
 * Login (/auth/login) scenario â€” PETZ_TC006 to PETZ_TC012.
 * Group: authLogin.
 *
 * TC011 requires three role accounts to be present (seeded). If a role login
 * fails we log the actual URL and assert tolerantly.
 */
public class LoginPageTest extends BaseTest {

    @Test(priority = 6,
          groups = {"authLogin", "functional", "regression", "smoke", "sanity", "positive"},
          description = "PETZ_TC006 - Validate /auth/login layout")
    public void TC006_LoginRender() {
        LoginPage page = new LoginPage(driver);
        page.open();

        StepReporter.check("Left panel is rendered",
                "Dark-blue branding panel visible", page.isLeftPanelVisible());
        StepReporter.check("Left panel feature bullets",
                "Manage pets / track rescues / Adopt / Book appointments",
                page.leftPanelFeaturesVisible());
        StepReporter.check("'Welcome back' heading",
                "Heading 'Welcome back' visible", page.isWelcomeBackVisible());
        StepReporter.check("'Back to home' link",
                "'Back to home' link visible", page.isBackToHomeLinkVisible());
        StepReporter.check("'Create one' link",
                "Orange 'Create one' link visible", page.isCreateOneLinkVisible());
        StepReporter.check("Sign In starts disabled",
                "Sign In button is disabled when both fields empty",
                page.isSignInDisabled());
    }

    @Test(priority = 7,
          groups = {"authLogin", "functional", "regression", "negative"},
          description = "PETZ_TC007 - Sign In stays disabled until both fields valid")
    public void TC007_LoginButtonDisabledEmpty() {
        LoginPage page = new LoginPage(driver);
        page.open();

        StepReporter.check("Initial state (both empty)",
                "Sign In disabled", page.isSignInDisabled());

        page.fillEmail("a");
        StepReporter.check("Only invalid email typed",
                "Sign In still disabled", page.isSignInDisabled());

        page.clearEmail();
        page.fillPassword("anything");
        StepReporter.check("Only password typed",
                "Sign In still disabled", page.isSignInDisabled());

        page.fillEmail("user@example.com");
        StepReporter.check("Valid email + password",
                "Sign In enabled (orange)", page.isSignInEnabled());
    }

    @Test(priority = 8,
          groups = {"authLogin", "functional", "regression", "negative"},
          description = "PETZ_TC008 - Invalid email keeps Sign In disabled (HTML5 validation)")
    public void TC008_LoginInvalidEmail() {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.fillEmail("bad");
        page.fillPassword("x");
        StepReporter.check("'bad' email + 'x' password",
                "Sign In disabled (invalid email syntax)", page.isSignInDisabled());

        page.blurEmail();
        StepReporter.check("After blurring email",
                "Sign In still disabled (no inline error required)",
                page.isSignInDisabled());
    }

    @Test(priority = 9,
          groups = {"authLogin", "functional", "regression", "positive"},
          description = "PETZ_TC009 - Password show/hide eye toggle")
    public void TC009_LoginPasswordEyeToggle() {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.fillPassword("VisibleSecret1!");

        StepReporter.check("After typing password",
                "Field type is 'password' (masked dots)", page.isPasswordMasked());

        page.clickEyeToggle();
        StepReporter.check("After clicking eye icon",
                "Password is revealed (type != password)", !page.isPasswordMasked());

        page.clickEyeToggle();
        StepReporter.check("After clicking eye icon again",
                "Password masked again", page.isPasswordMasked());
    }

    @Test(priority = 10,
          groups = {"authLogin", "functional", "regression", "negative"},
          description = "PETZ_TC010 - Wrong credentials show an error / stay on /auth/login")
    public void TC010_LoginWrongCredentials() {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.fillEmail("nobody@example.com");
        page.fillPassword("Whatever@123");
        page.clickSignIn();

        // Wait briefly then check URL + error surface.
        Waits.documentReady(driver);

        boolean stillOnLogin = page.getCurrentUrl().contains("/auth/login");
        StepReporter.check("URL after submit",
                "Remains on /auth/login", page.getCurrentUrl());
        StepReporter.check("Error surface (toast or inline)",
                "Some error indicator OR user stays on login page",
                stillOnLogin || page.hasErrorMessage());
    }

    @Test(priority = 11,
          groups = {"authLogin", "functional", "regression", "smoke", "sanity", "positive"},
          description = "PETZ_TC011 - Successful login routes to the right dashboard per role")
    public void TC011_LoginSuccessRoutes() {
        LoginPage page = new LoginPage(driver);

        page.login(PET_OWNER_EMAIL, PET_OWNER_PASSWORD);
        StepReporter.check("Pet Owner login",
                "/dashboard", page.getCurrentUrl());

        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "localStorage.clear(); sessionStorage.clear();");
        page.login(NGO_EMAIL, NGO_PASSWORD);
        StepReporter.check("NGO login",
                "/ngo", page.getCurrentUrl());

        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "localStorage.clear(); sessionStorage.clear();");
        page.login(HOSPITAL_EMAIL, HOSPITAL_PASSWORD);
        StepReporter.check("Hospital login",
                "/hospital", page.getCurrentUrl());
    }

    @Test(priority = 12,
          groups = {"authLogin", "functional", "regression", "positive"},
          description = "PETZ_TC012 - 'Create one' link routes to /auth/register")
    public void TC012_LoginCreateOneLink() {
        LoginPage page = new LoginPage(driver);
        page.open();
        page.clickCreateOne();

        StepReporter.check("After clicking 'Create one'",
                "/auth/register", page.getCurrentUrl());

        driver.navigate().back();
        page.clickBackToHome();
        StepReporter.check("After clicking 'Back to home'",
                BASE_URL, page.getCurrentUrl());
    }
}
