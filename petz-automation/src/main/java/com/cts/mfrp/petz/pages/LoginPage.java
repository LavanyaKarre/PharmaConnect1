package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.base.WaitUtil;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.constants.UserRole;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * Login form at /auth/login.
 */
public class LoginPage extends BasePage {

    private static final By EMAIL_INPUT    = By.cssSelector("input[type='email'], input[formcontrolname='email'], input[name='email']");
    private static final By PASSWORD_INPUT = By.cssSelector("input[type='password'], input[formcontrolname='password']");
    private static final By EYE_TOGGLE     = By.xpath("//*[contains(@class,'visibility') or contains(@class,'eye') or self::mat-icon[contains(text(),'visibility')]]");
    private static final By SIGN_IN_BUTTON = By.xpath("//button[contains(normalize-space(),'Sign In') or contains(normalize-space(),'Login') or contains(normalize-space(),'Log In')]");
    private static final By ERROR_TOAST    = By.cssSelector(".mat-mdc-snack-bar-label, .error, [class*='error']");
    private static final By EMAIL_ERROR    = By.xpath("//mat-error[contains(.,'mail')] | //*[contains(@class,'error') and contains(.,'mail')]");
    private static final By PASSWORD_ERROR = By.xpath("//mat-error[contains(.,'assword')] | //*[contains(@class,'error') and contains(.,'assword')]");
    private static final By CREATE_ACCOUNT_LINK = By.xpath("//a[@routerlink='/auth/register' or contains(.,'Create') or contains(.,'Sign up')]");

    public LoginPage open() {
        goTo(AppConstants.LOGIN_URL);
        return this;
    }

    public LoginPage fillEmail(String value)    { type(EMAIL_INPUT, value); return this; }
    public LoginPage fillPassword(String value) { type(PASSWORD_INPUT, value); return this; }

    public LoginPage blurEmail() {
        WebElement el = find(EMAIL_INPUT);
        el.sendKeys(Keys.TAB);
        shortPause();
        return this;
    }

    public LoginPage blurPassword() {
        WebElement el = find(PASSWORD_INPUT);
        el.sendKeys(Keys.TAB);
        shortPause();
        return this;
    }

    public void clickEyeToggle() { click(EYE_TOGGLE); }
    public void clickSignIn()    { click(SIGN_IN_BUTTON); }
    public void clickCreateAccount() { click(CREATE_ACCOUNT_LINK); }

    public boolean isSignInDisabled() {
        String disabled = attributeOf(SIGN_IN_BUTTON, "disabled");
        return disabled != null && !"false".equals(disabled);
    }

    public boolean isSignInEnabled() { return !isSignInDisabled(); }

    public boolean isErrorVisible() {
        return isPresent(ERROR_TOAST) || isPresent(EMAIL_ERROR) || isPresent(PASSWORD_ERROR);
    }

    public String getEmailError() {
        return isPresent(EMAIL_ERROR) ? textOf(EMAIL_ERROR) : "";
    }

    public String getPasswordError() {
        return isPresent(PASSWORD_ERROR) ? textOf(PASSWORD_ERROR) : "";
    }

    public String getErrorText() {
        if (isPresent(ERROR_TOAST))    return textOf(ERROR_TOAST);
        if (isPresent(EMAIL_ERROR))    return textOf(EMAIL_ERROR);
        if (isPresent(PASSWORD_ERROR)) return textOf(PASSWORD_ERROR);
        return "";
    }

    public String getPasswordInputType() {
        return attributeOf(PASSWORD_INPUT, "type");
    }

    /** Submits the form and waits up to 30 s for a URL change away from /auth/login. */
    public void submitAndWait() {
        click(SIGN_IN_BUTTON);
        try {
            WaitUtil.wait(driver, AppConstants.LOGIN_WAIT)
                    .until(d -> !d.getCurrentUrl().contains("/auth/login"));
        } catch (Exception ignored) {
            // either form rejected the submission (stay on /auth/login) or a slow login
        }
    }

    /** Logs in as the seed user for the given role. */
    public void loginAs(UserRole role) {
        switch (role) {
            case PET_OWNER:
                fillEmail(AppConstants.PET_OWNER_EMAIL);
                fillPassword(AppConstants.PET_OWNER_PASSWORD);
                break;
            case NGO:
                fillEmail(AppConstants.NGO_EMAIL);
                fillPassword(AppConstants.NGO_PASSWORD);
                break;
            case HOSPITAL:
                fillEmail(AppConstants.HOSPITAL_EMAIL);
                fillPassword(AppConstants.HOSPITAL_PASSWORD);
                break;
            default:
                throw new IllegalArgumentException("Role does not have seed credentials: " + role);
        }
        submitAndWait();
    }
}
