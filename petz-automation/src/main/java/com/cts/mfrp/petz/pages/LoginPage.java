package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.*;

/**
 * /auth/login. Covers PETZ_TC006 – PETZ_TC012.
 *
 * <p>Selectors verified against the live deployment: inputs are matInput with
 * formcontrolname, the password show/hide is a mat-icon-button with
 * title="Show password" / "Hide password", and Back-to-home is &lt;a routerlink="/"&gt;.
 */
public class LoginPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // Inputs
    private final By emailInput    = By.xpath("//input[@formcontrolname='email']");
    private final By passwordInput = By.xpath("//input[@formcontrolname='password']");
    private final By submitButton  = By.xpath("//button[@type='submit' and (normalize-space()='Sign In' or normalize-space()='Login' or normalize-space()='Log In')]");

    // Layout
    private final By leftPanelLogo  = By.cssSelector(".auth-left .brand, .auth-left [class*='logo'], .auth-left");
    private final By welcomeBackH2  = By.xpath("//h2[normalize-space()='Welcome back']");
    private final By backToHomeLink = By.xpath("//a[@routerlink='/' and contains(normalize-space(),'Back to home')]");
    private final By createOneLink  = By.xpath("//a[@routerlink='/auth/register']");

    // Eye toggle (its title flips between Show/Hide on click)
    private final By eyeToggleButton = By.xpath(
            "//button[@matsuffix and (@title='Show password' or @title='Hide password')]");

    // Error surface (snack-bar / inline). Site can use either pattern.
    private final By errorMessage = By.xpath(
            "//*[contains(@class,'mat-mdc-snack-bar-label') or contains(@class,'snack-bar') " +
            "or contains(@class,'error-message') or (contains(@class,'mat-mdc-form-field-error'))]");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(LOGIN_URL); }

    // ── login helpers (existing NotificationsTest depends on loginAsPetOwner) ──
    public void loginAsPetOwner() { login(PET_OWNER_EMAIL, PET_OWNER_PASSWORD); }
    public void loginAsNgo()      { login(NGO_EMAIL,       NGO_PASSWORD); }
    public void loginAsHospital() { login(HOSPITAL_EMAIL,  HOSPITAL_PASSWORD); }

    public void login(String email, String password) {
        open();
        fillEmail(email);
        fillPassword(password);
        clickSignIn();
        // Use a longer wait than EXPLICIT_WAIT — Railway backend can take >15s during cold
        // starts, which was the root cause of several intermittent TC0XX-via-login timeouts.
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.urlMatches(".+/(dashboard|ngo|hospital).*"));
    }

    // ── TC007 / TC008: progressive enable ──
    public void fillEmail(String email) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        el.clear(); el.sendKeys(email);
    }

    public void fillPassword(String password) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        el.clear(); el.sendKeys(password);
    }

    public void clearEmail()    { try { driver.findElement(emailInput).clear(); }    catch (Exception ignored) {} }
    public void clearPassword() { try { driver.findElement(passwordInput).clear(); } catch (Exception ignored) {} }

    public void blurEmail() {
        try { ((JavascriptExecutor) driver).executeScript("arguments[0].blur();",
                driver.findElement(emailInput)); } catch (Exception ignored) {}
    }

    public void clickSignIn() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(submitButton));
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    public boolean isSignInDisabled() {
        try {
            WebElement btn = driver.findElement(submitButton);
            String disabled = btn.getAttribute("disabled");
            return (disabled != null && !disabled.equals("false"));
        } catch (Exception e) { return true; }
    }

    public boolean isSignInEnabled() { return !isSignInDisabled(); }

    // ── TC006: layout assertions ──
    public boolean isLeftPanelVisible()    { return isVisible(leftPanelLogo); }
    public boolean isWelcomeBackVisible()  { return isVisible(welcomeBackH2); }
    public boolean isBackToHomeLinkVisible() { return isVisible(backToHomeLink); }
    public boolean isCreateOneLinkVisible()  { return isVisible(createOneLink); }

    public boolean leftPanelFeaturesVisible() {
        String src = driver.getPageSource();
        return src.contains("Manage your pets") && src.contains("track rescues")
                && src.contains("Adopt animals") && src.contains("Book vet appointments");
    }

    // ── TC009: password show/hide toggle ──
    public boolean isPasswordMasked() {
        try { return "password".equals(driver.findElement(passwordInput).getAttribute("type")); }
        catch (Exception e) { return true; }
    }

    public void clickEyeToggle() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(eyeToggleButton));
        try { btn.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);
        }
    }

    // ── TC010 / TC019: error surface ──
    public boolean hasErrorMessage() {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage)).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    // ── TC012: navigation links ──
    public void clickCreateOne()  { safeClick(createOneLink); }
    public void clickBackToHome() { safeClick(backToHomeLink); }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    // ── helpers ──
    private boolean isVisible(By by) {
        try { return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).isDisplayed(); }
        catch (Exception e) { return false; }
    }

    private void safeClick(By by) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(by));
        try { el.click(); } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
}
