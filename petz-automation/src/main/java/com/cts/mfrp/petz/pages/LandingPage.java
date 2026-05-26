package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * Public landing page at "/".
 */
public class LandingPage extends BasePage {

    private static final By LOGO          = By.cssSelector(".nav-logo-text, .footer-logo-text");
    private static final By HERO_TITLE    = By.xpath("//section[contains(@class,'hero')]//*[self::h1 or self::h2]");
    private static final By STATS_STRIP   = By.cssSelector(".stats, .stats-strip, [class*='stat']");
    private static final By FEATURE_CARDS = By.cssSelector(".features, .feature-card, [class*='feature']");
    private static final By FOOTER        = By.tagName("footer");

    // CTAs and nav links - allow both <a> and <button>, and check for partial match
    private static final By SIGN_UP_FREE   = By.xpath("//*[self::a or self::button][contains(normalize-space(),'Sign Up') or contains(normalize-space(),'Sign up')]");
    private static final By LOG_IN         = By.xpath("//*[self::a or self::button][contains(normalize-space(),'Log In') or contains(normalize-space(),'Login') or contains(normalize-space(),'Sign In')]");
    private static final By GET_STARTED    = By.xpath("//*[self::a or self::button][contains(normalize-space(),'Get Started')]");
    private static final By NAV_FEATURES   = By.xpath("//nav//a[contains(normalize-space(),'Features')]");
    private static final By NAV_HOW        = By.xpath("//nav//a[contains(normalize-space(),'How it Works') or contains(normalize-space(),'How It Works')]");
    private static final By NAV_CITIES     = By.xpath("//nav//a[contains(normalize-space(),'Cities')]");
    private static final By BOTTOM_SIGN_IN = By.xpath("(//footer//a | //*[contains(@class,'bottom')]//a)[contains(normalize-space(),'Sign in') or contains(normalize-space(),'Log in')]");

    public LandingPage open() {
        goTo(AppConstants.HOME_URL);
        return this;
    }

    public boolean isLogoVisible()         { return isVisible(LOGO); }
    public boolean isHeroVisible()         { return isVisible(HERO_TITLE); }
    public boolean isStatsVisible()        { return isPresent(STATS_STRIP); }
    public boolean isFeaturesVisible()     { return isPresent(FEATURE_CARDS); }
    public boolean isFooterVisible()       { return isVisible(FOOTER); }

    public void clickSignUpFree()    { click(SIGN_UP_FREE); }
    public void clickLogIn()         { click(LOG_IN); }
    public void clickGetStarted()    { click(GET_STARTED); }
    public void clickNavFeatures()   { click(NAV_FEATURES); }
    public void clickNavHow()        { click(NAV_HOW); }
    public void clickNavCities()     { click(NAV_CITIES); }
    public void clickBottomSignIn()  { click(BOTTOM_SIGN_IN); }

    /** Click any CTA by its visible label (used by TC002 data-driven). */
    public void clickByLabel(String label) {
        switch (label.toLowerCase()) {
            case "sign up free":   clickSignUpFree(); break;
            case "log in":         clickLogIn(); break;
            case "get started":    clickGetStarted(); break;
            case "features":       clickNavFeatures(); break;
            case "how it works":   clickNavHow(); break;
            case "cities":         clickNavCities(); break;
            case "bottom sign in": clickBottomSignIn(); break;
            default: throw new IllegalArgumentException("Unknown landing CTA: " + label);
        }
    }
}
