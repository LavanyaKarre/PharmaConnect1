package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;

/**
 * Registration form at /auth/register.
 */
public class RegisterPage extends BasePage {

    private static final By FULL_NAME     = By.cssSelector("input[formcontrolname='fullName'], input[name='fullName'], input[name='name']");
    private static final By PHONE         = By.cssSelector("input[formcontrolname='phone'], input[name='phone'], input[type='tel']");
    private static final By EMAIL         = By.cssSelector("input[type='email'], input[formcontrolname='email']");
    private static final By PASSWORD      = By.cssSelector("input[formcontrolname='password']");
    private static final By CONFIRM       = By.cssSelector("input[formcontrolname='confirmPassword'], input[formcontrolname='confirm']");
    private static final By ACCOUNT_TYPE  = By.cssSelector("mat-select[formcontrolname='role']");
    private static final By CREATE_BUTTON = By.xpath("//button[contains(normalize-space(),'Create Account') or contains(normalize-space(),'Create') or contains(normalize-space(),'Register') or contains(normalize-space(),'Sign Up')]");
    private static final By ANY_ERROR     = By.cssSelector("mat-error, .error, .mat-mdc-snack-bar-label");

    public RegisterPage open() {
        goTo(AppConstants.REGISTER_URL);
        return this;
    }

    public RegisterPage fillFullName(String v) { type(FULL_NAME, v); return this; }
    public RegisterPage fillPhone(String v)    { type(PHONE, v); return this; }
    public RegisterPage fillEmail(String v)    { type(EMAIL, v); return this; }
    public RegisterPage fillPassword(String v) { type(PASSWORD, v); return this; }
    public RegisterPage fillConfirm(String v)  { type(CONFIRM, v); return this; }

    public RegisterPage selectAccountType(String role) {
        if (!isPresent(ACCOUNT_TYPE)) return this;
        WebElement el = find(ACCOUNT_TYPE);
        if ("select".equalsIgnoreCase(el.getTagName())) {
            selectByVisibleText(ACCOUNT_TYPE, role);
        } else {
            click(ACCOUNT_TYPE);
            shortPause();
            click(By.xpath("//mat-option//span[contains(normalize-space(),'" + role + "')]"));
        }
        return this;
    }

    public RegisterPage blurField(String field) {
        By target = locatorFor(field);
        WebElement el = find(target);
        el.sendKeys(Keys.TAB);
        shortPause();
        return this;
    }

    public RegisterPage fillField(String field, String value) {
        if ("accountType".equalsIgnoreCase(field)) {
            selectAccountType(value);
        } else {
            type(locatorFor(field), value);
        }
        return this;
    }

    public void clickCreate() { click(CREATE_BUTTON); }

    public boolean isCreateDisabled() {
        String disabled = attributeOf(CREATE_BUTTON, "disabled");
        return disabled != null && !"false".equals(disabled);
    }

    public boolean isCreateEnabled() { return !isCreateDisabled(); }

    public String getAnyError() {
        return isPresent(ANY_ERROR) ? textOf(ANY_ERROR) : "";
    }

    public boolean isAnyErrorVisible() { return isPresent(ANY_ERROR); }

    private By locatorFor(String field) {
        switch (field.toLowerCase()) {
            case "fullname":         return FULL_NAME;
            case "phone":            return PHONE;
            case "email":            return EMAIL;
            case "password":         return PASSWORD;
            case "confirmpassword":  return CONFIRM;
            case "accounttype":      return ACCOUNT_TYPE;
            default: throw new IllegalArgumentException("Unknown register field: " + field);
        }
    }
}
