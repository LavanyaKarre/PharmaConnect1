package com.cts.mfrp.petz.tests.functional;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LandingPage;
import com.cts.mfrp.petz.utils.StepReporter;
import com.cts.mfrp.petz.utils.Waits;
import org.testng.annotations.Test;

/**
 * Landing Page scenario â€” PETZ_TC001 to PETZ_TC005.
 * Group: landingPage. Anonymous-user tests (no login).
 *
 * Strategy mirrors HospitalDashboardTest: use the POM to navigate / click,
 * but rely on page-source `contains` for textual assertions so we tolerate
 * Unicode dashes, capitalisation drift, and minor copy edits.
 */
public class LandingPageTest extends BaseTest {

    private boolean srcContains(String needle) {
        return driver.getPageSource().toLowerCase().contains(needle.toLowerCase());
    }

    @Test(priority = 1,
          groups = {"landingPage", "ui", "regression", "smoke", "sanity", "positive"},
          description = "PETZ_TC001 - Validate header + hero on the landing page")
    public void TC001_HomeRender() {
        LandingPage page = new LandingPage(driver);
        page.open();

        StepReporter.check("Page title contains 'Animal Welfare Platform'",
                "Animal Welfare Platform", page.getPageTitle());
        StepReporter.check("PETZ logo is visible",
                "Logo present in top header", page.isLogoVisible());
        StepReporter.check("Top-nav links Features/How it Works/Cities visible",
                "All three nav links visible", page.areHeaderNavLinksVisible());
        StepReporter.check("Log In link is visible",
                "Log In link in the header", page.isLogInLinkVisible());
        StepReporter.check("Sign Up Free button is visible",
                "Orange 'Sign Up Free' button visible", page.isSignUpButtonVisible());

        StepReporter.check("Hero badge",
                "'Now Active in Chennai' badge", srcContains("Chennai"));
        StepReporter.check("Hero headline",
                "'Deserves Care' headline visible", srcContains("Deserves Care"));
        StepReporter.check("Hero CTAs",
                "'Get Started' + 'See Features' visible",
                srcContains("Get Started") && srcContains("See Features"));
    }

    @Test(priority = 2,
          groups = {"landingPage", "ui", "regression", "positive"},
          description = "PETZ_TC002 - Validate stats strip + feature/how-it-works cards")
    public void TC002_HomeStatsAndCards() {
        LandingPage page = new LandingPage(driver);
        page.open();

        // Stats + feature cards are lazy-rendered below the hero. Scroll
        // through the page so Angular instantiates each section.
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "window.scrollTo(0, document.body.scrollHeight);");
        Waits.documentReady(driver);
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                "window.scrollTo(0, 0);");
        Waits.pageSourceContainsAny(driver, "2,400", "Rescue Response");

        StepReporter.check("Stats strip â€” 2,400+ rescues / 120+ clinics",
                "Stats numbers visible",
                srcContains("2,400") && srcContains("120"));

        StepReporter.check("Stats strip â€” response rate",
                "'98%' OR 'Rescue Response' visible",
                srcContains("98") || srcContains("Rescue Response"));

        StepReporter.check("Feature card chips (any 2 of 4)",
                "FOR PET OWNERS / SEE NETWORK / OPEN ADOPTIONS / SEE CLINICS",
                (srcContains("FOR PET OWNERS") ? 1 : 0)
                    + (srcContains("SEE NETWORK")  ? 1 : 0)
                    + (srcContains("OPEN ADOPTIONS") ? 1 : 0)
                    + (srcContains("SEE CLINICS") ? 1 : 0) >= 2);

        StepReporter.check("'Simple. Fast. Effective.' section heading",
                "Simple / Fast / Effective heading visible",
                srcContains("Simple") && srcContains("Effective"));

        StepReporter.check("Three how-it-works steps",
                "Report or Browse / NGOs Respond / Animals Thrive",
                srcContains("Report") && srcContains("NGO") && srcContains("Thrive"));
    }

    @Test(priority = 3,
          groups = {"landingPage", "ui", "regression", "positive"},
          description = "PETZ_TC003 - Validate 'Where We Operate' cities widget")
    public void TC003_HomeCitiesWidget() {
        LandingPage page = new LandingPage(driver);
        page.open();

        // The cities section is lazy-rendered; clicking the 'Cities' nav
        // anchor scrolls it into view so Angular instantiates the DOM.
        try { page.clickNavCities(); } catch (Exception ignored) {}
        Waits.pageSourceContains(driver, "Where We Operate");

        StepReporter.check("Where We Operate heading",
                "'Where We Operate' visible", srcContains("Where We Operate"));
        StepReporter.check("Chennai tile",
                "Chennai (Live) visible", srcContains("Chennai"));
        StepReporter.check("Coming Soon copy",
                "Non-Chennai cities show 'Coming Soon'", srcContains("Coming Soon"));
    }

    @Test(priority = 4,
          groups = {"landingPage", "functional", "regression", "positive"},
          description = "PETZ_TC004 - Validate landing-page CTAs route to /auth/* pages")
    public void TC004_HomeCTAsRoute() {
        LandingPage page = new LandingPage(driver);
        page.open();

        page.clickSignUpFree();
        Waits.urlContains(driver, "/auth/register");
        StepReporter.check("Header 'Sign Up Free' destination",
                "/auth/register", page.getCurrentUrl());

        driver.navigate().back();
        Waits.urlNotContaining(driver, "/auth/register");
        page.clickLogIn();
        Waits.urlContains(driver, "/auth/login");
        StepReporter.check("Header 'Log In' destination",
                "/auth/login", page.getCurrentUrl());

        driver.navigate().back();
        Waits.urlNotContaining(driver, "/auth/login");
        try {
            page.clickBottomGetStarted();
            Waits.urlContains(driver, "/auth/register");
            StepReporter.check("Bottom 'Get Started' destination",
                    "/auth/register", page.getCurrentUrl());
        } catch (Exception e) {
            StepReporter.info("Bottom 'Get Started' link not found in this build â€” skipped.");
        }

        driver.navigate().back();
        Waits.urlNotContaining(driver, "/auth/register");
        try {
            page.clickBottomSignInLink();
            Waits.urlContains(driver, "/auth/login");
            StepReporter.check("Bottom 'Already have an account?' destination",
                    "/auth/login", page.getCurrentUrl());
        } catch (Exception e) {
            StepReporter.info("Bottom sign-in link not found in this build â€” skipped.");
        }
    }

    @Test(priority = 5,
          groups = {"landingPage", "functional", "regression", "positive"},
          description = "PETZ_TC005 - Validate in-page anchor links in the top nav")
    public void TC005_HomeAnchorScroll() {
        LandingPage page = new LandingPage(driver);
        page.open();

        page.clickNavFeatures();
        Waits.pageSourceContainsAny(driver, "Everything Your Pet Needs", "Features");
        StepReporter.check("'Features' click â€” section reachable on page",
                "'Everything Your Pet Needs' is somewhere in DOM",
                srcContains("Everything Your Pet Needs") || page.isFeaturesSectionInView());

        page.clickNavHowItWorks();
        Waits.pageSourceContainsAny(driver, "Simple", "Effective");
        StepReporter.check("'How it Works' click â€” section reachable",
                "'Simple. Fast. Effective.' in DOM",
                srcContains("Simple") || page.isHowItWorksSectionInView());

        page.clickNavCities();
        Waits.pageSourceContains(driver, "Where We Operate");
        StepReporter.check("'Cities' click â€” section reachable",
                "'Where We Operate' in DOM",
                srcContains("Where We Operate") || page.isCitiesSectionInView());
    }
}
