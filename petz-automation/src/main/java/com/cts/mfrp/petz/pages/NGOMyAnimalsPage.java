package com.cts.mfrp.petz.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static com.cts.mfrp.petz.constants.AppConstants.EXPLICIT_WAIT;
import static com.cts.mfrp.petz.constants.AppConstants.NGO_ANIMALS_URL;

/**
 * /ngo/animals. Covers PETZ_TC063 – PETZ_TC065.
 */
public class NGOMyAnimalsPage {

    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By title    = By.xpath("//*[self::h1 or self::h2 or self::h3][contains(normalize-space(),'My Animals')]");
    private final By subtitle = By.xpath("//*[contains(normalize-space(),'Manage all animals listed for adoption')]");

    private final By addAnimalBtnTop   = By.xpath(
            "(//a[contains(normalize-space(),'Add Animal')] | //button[contains(normalize-space(),'Add Animal')])[1]");
    private final By addFirstAnimalBtn = By.xpath(
            "//a[contains(normalize-space(),'Add your first animal')] | //button[contains(normalize-space(),'Add your first animal')]");

    private final By searchInput = By.xpath(
            "//input[contains(@placeholder,'Search') and (contains(@placeholder,'name') or contains(@placeholder,'breed'))]");

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

    // Add-animal form (after CTA click)
    private final By formNameInput    = By.xpath("//input[@formcontrolname='name' or @name='name']");
    private final By formSpeciesInput = By.xpath("//input[@formcontrolname='species'] | //mat-select[@formcontrolname='species']");
    private final By formBreedInput   = By.xpath("//input[@formcontrolname='breed' or @name='breed']");
    private final By formAgeInput     = By.xpath("//input[@formcontrolname='ageYears' or @formcontrolname='age' or @name='age']");
    private final By formGenderInput  = By.xpath("//mat-select[@formcontrolname='gender'] | //input[@formcontrolname='gender']");
    private final By formCityInput    = By.xpath("//input[@formcontrolname='city' or @name='city']");

    public NGOMyAnimalsPage(WebDriver driver) {
        this.driver = driver;
        this.wait   = new WebDriverWait(driver, Duration.ofSeconds(EXPLICIT_WAIT));
    }

    public void open() { driver.get(NGO_ANIMALS_URL); }

    public boolean isTitleVisible()    { return isVisible(title); }
    public boolean isSubtitleVisible() { return isVisible(subtitle); }

    public boolean isAddAnimalBtnVisible() { return isVisible(addAnimalBtnTop); }
    public boolean isSearchInputVisible()  { return isVisible(searchInput); }

    public boolean areFiltersVisible() {
        return isVisible(speciesFilter) && isVisible(statusFilter) && isVisible(sortFilter);
    }

    public boolean isEmptyStateVisible() { return isVisible(emptyStateTitle); }

    public void clickAddAnimalTop()   { safeClick(addAnimalBtnTop); }
    public void clickAddFirstAnimal() { safeClick(addFirstAnimalBtn); }

    public List<String> openSpeciesOptions() { return openOptions(speciesFilter); }
    public List<String> openStatusOptions()  { return openOptions(statusFilter); }
    public List<String> openSortOptions()    { return openOptions(sortFilter); }

    public boolean isAddAnimalFormVisible() {
        return isVisible(formNameInput);
    }

    public boolean addAnimalFormHasExpectedFields() {
        boolean hasName    = !driver.findElements(formNameInput).isEmpty();
        boolean hasSpecies = !driver.findElements(formSpeciesInput).isEmpty();
        boolean hasBreed   = !driver.findElements(formBreedInput).isEmpty();
        boolean hasAge     = !driver.findElements(formAgeInput).isEmpty();
        boolean hasGender  = !driver.findElements(formGenderInput).isEmpty();
        boolean hasCity    = !driver.findElements(formCityInput).isEmpty();
        return hasName && hasSpecies && hasBreed && hasAge && hasGender && hasCity;
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
