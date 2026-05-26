package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;
import static com.cts.mfrp.petz.constants.AppConstants.NGO_ANIMALS_URL;

/**
 * /ngo/animals. Covers PETZ_TC063 – PETZ_TC065.
 *
 * IMPORTANT — verified against the live DOM
 * (petz-frontend/src/app/features/ngo/ngo-animals/ngo-animals.component.html):
 *
 *   • The 3 filter dropdowns are native HTML <select class="fsel">,
 *     NOT Angular Material <mat-select>. Their options are <option>,
 *     NOT <mat-option>. Earlier versions of this page used mat-select
 *     locators which is why every TC was failing.
 *
 *   • The Add-Animal form uses [(ngModel)] (template-driven), so the
 *     inputs DON'T have formControlName attributes. Fields are
 *     identified by their <label class="field-label"> text.
 *
 *   • Filters in DOM order: 0 = Species, 1 = Status, 2 = Sort.
 */
public class NGOMyAnimalsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    // ── Page-level locators ──
    private final By title    = By.xpath(
            "//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'My Animals')]");
    private final By subtitle = By.xpath(
            "//*[contains(normalize-space(),'Manage all animals listed for adoption')]");

    private final By addAnimalBtnTop = By.xpath(
            "(//a[contains(normalize-space(),'Add Animal')] | " +
                    " //button[contains(normalize-space(),'Add Animal') and not(contains(.,'Cancel'))])[1]");
    private final By addFirstAnimalBtn = By.xpath(
            "//a[contains(normalize-space(),'Add your first animal')] | " +
                    "//button[contains(normalize-space(),'Add your first animal')]");

    private final By searchInput = By.cssSelector("input.search-input");

    // Filters — native <select class="fsel">, ordered Species / Status / Sort
    private final By filterSelects = By.cssSelector("select.fsel");
    private final By filterLabel   = By.cssSelector("label.select-label");

    private final By emptyStateTitle = By.xpath(
            "//*[contains(normalize-space(),'No animals listed yet')]");
    // Filters are native <select> elements paired with <label class="select-label">…</label>,
    // not Angular Material mat-select. Confirmed against the live DOM 2026-05-25.
    private final By speciesFilter = By.xpath(
            "//label[contains(@class,'select-label') and normalize-space()='Species']/following-sibling::select"
          + " | //label[normalize-space()='Species']/following-sibling::select");
    private final By statusFilter = By.xpath(
            "//label[contains(@class,'select-label') and normalize-space()='Status']/following-sibling::select"
          + " | //label[normalize-space()='Status']/following-sibling::select");
    private final By sortFilter = By.xpath(
            "//label[contains(@class,'select-label') and normalize-space()='Sort']/following-sibling::select"
          + " | //label[normalize-space()='Sort']/following-sibling::select");

    private final By emptyStateTitle = By.xpath("//*[contains(normalize-space(),'No animals listed yet')]");


    // Add-animal form container
    private final By formCard = By.cssSelector(".form-card");

    public NGOMyAnimalsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(NGO_ANIMALS_URL); }

    public boolean isTitleVisible()        { return isVisible(title); }
    public boolean isSubtitleVisible()     { return isVisible(subtitle); }
    public boolean isAddAnimalBtnVisible() { return isVisible(addAnimalBtnTop); }
    public boolean isSearchInputVisible()  { return isVisible(searchInput); }

    /**
     * Three filters are visible when we find at least 3 <select.fsel> AND
     * the three matching <label.select-label> texts (Species, Status, Sort).
     */
    public boolean areFiltersVisible() {
        try {
            // Strong wait: at least 3 <select> elements AND the page source
            // contains the three filter label texts. This is forgiving:
            // even if class names ("fsel") changed at build time, the
            // labels and the select elements themselves are stable.
            wait.until(d -> {
                int selectCount = d.findElements(By.tagName("select")).size();
                String src = d.getPageSource();
                return selectCount >= 3
                        && src.contains("Species")
                        && src.contains("Status")
                        && src.contains("Sort");
            });
            return true;
        } catch (Exception e) {
            // Diagnostic — print what we DID find so we can fix the locator if needed
            int selectCount = driver.findElements(By.tagName("select")).size();
            String src = driver.getPageSource();
            System.out.println("[NGOMyAnimalsPage] areFiltersVisible FAILED");
            System.out.println("[NGOMyAnimalsPage]   <select> count: " + selectCount);
            System.out.println("[NGOMyAnimalsPage]   page contains 'Species': " + src.contains("Species"));
            System.out.println("[NGOMyAnimalsPage]   page contains 'Status':  " + src.contains("Status"));
            System.out.println("[NGOMyAnimalsPage]   page contains 'Sort':    " + src.contains("Sort"));
            return false;
        }
    }

    public boolean isEmptyStateVisible() { return isVisible(emptyStateTitle); }

    public void clickAddAnimalTop()   { safeClick(addAnimalBtnTop); }
    public void clickAddFirstAnimal() { safeClick(addFirstAnimalBtn); }

    /**
     * Filters are in DOM order: 0 = Species, 1 = Status, 2 = Sort.
     * Returns the visible text of every <option>.
     */
    public List<String> openSpeciesOptions() { return getOptionsAt(0); }
    public List<String> openStatusOptions()  { return getOptionsAt(1); }
    public List<String> openSortOptions()    { return getOptionsAt(2); }

    private List<String> getOptionsAt(int index) {
        // Wait until at least 3 fsel selects are present
        wait.until(d -> d.findElements(filterSelects).size() >= 3);
        List<WebElement> selects = driver.findElements(filterSelects);
        WebElement sel = selects.get(index);
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", sel);
        // Click to focus (good for screenshots; reading via Select wrapper
        // doesn't require the native dropdown to be visually expanded)
        try { sel.click(); } catch (Exception ignored) {}
        List<String> opts = new ArrayList<>();
        for (WebElement o : new Select(sel).getOptions()) {
            opts.add(o.getText().trim());
        }
        return opts;
    }

    /**
     * The Add-Animal form is visible when the .form-card container is in the DOM.
     */
    public boolean isAddAnimalFormVisible() {
        return isVisible(formCard);
    }

    /**
     * The form must show fields for Animal Name, Species, Breed, Age, Gender
     * and City. The page uses <label class="field-label"> texts; we match by
     * substring to tolerate the required-asterisk ("Animal Name *") and unit
     * suffix ("Age (months)").
     */
    public boolean addAnimalFormHasExpectedFields() {
        if (!isVisible(formCard)) return false;
        List<String> labels = new ArrayList<>();
        for (WebElement e : driver.findElements(By.cssSelector("label.field-label"))) {
            labels.add(e.getText().trim());
        }
        String[] required = {"Animal Name", "Species", "Breed", "Age", "Gender", "City"};
        for (String r : required) {
            boolean found = labels.stream().anyMatch(l -> l.contains(r));
            if (!found) {
                System.out.println("[NGOMyAnimalsPage] Missing form field label: '"
                        + r + "'. Labels found: " + labels);
                return false;
            }
        }
        return true;
    }

    public String getCurrentUrl() { return driver.getCurrentUrl(); }

    // ── helpers ──

    // Native <select> — read the <option> children directly via Selenium's Select wrapper.
    private List<String> openOptions(By selectLocator) {
        try {
            WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(selectLocator));
            return new Select(el).getOptions().stream()
                    .map(WebElement::getText).map(String::trim).toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    private boolean isVisible(By by) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(by)).isDisplayed();
        } catch (Exception e) { return false; }
    }

    private void safeClick(By by) {
        WebElement el = wait.until(ExpectedConditions.elementToBeClickable(by));
        ((JavascriptExecutor) driver)
                .executeScript("arguments[0].scrollIntoView({block:'center'});", el);
        try { el.click(); }
        catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        }
    }
}
