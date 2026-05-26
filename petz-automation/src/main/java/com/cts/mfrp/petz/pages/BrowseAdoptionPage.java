package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Browse adoption animals at /adoption/animals.
 */
public class BrowseAdoptionPage extends BasePage {

    private static final By PAGE_TITLE       = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(),'Browse') or contains(normalize-space(),'Adoption')]");
    private static final By SPECIES_SELECT   = By.cssSelector("select[formcontrolname='species'], select[name='species']");
    private static final By CITY_INPUT       = By.cssSelector("input[formcontrolname='city'], input[name='city'], input[placeholder*='City']");
    private static final By SEARCH_BUTTON    = By.xpath("//button[contains(normalize-space(),'Search')]");
    private static final By PET_CARDS        = By.cssSelector(".pet-card, [class*='pet-card'], mat-card");
    private static final By AVAILABLE_CHIP   = By.xpath("//*[contains(normalize-space(),'AVAILABLE') or contains(normalize-space(),'Available')]");
    private static final By NO_RESULTS       = By.xpath("//*[contains(normalize-space(),'No results') or contains(normalize-space(),'No pets found') or contains(normalize-space(),'No animals')]");
    private static final By MY_APPS_BUTTON   = By.xpath("//*[self::a or self::button][contains(normalize-space(),'My Applications')]");

    public BrowseAdoptionPage open() {
        goTo(AppConstants.ADOPTION_ANIMALS_URL);
        return this;
    }

    public boolean isTitleVisible() { return isPresent(PAGE_TITLE); }

    public void selectSpecies(String species) {
        WebElement el = find(SPECIES_SELECT);
        if ("select".equalsIgnoreCase(el.getTagName())) {
            new Select(el).selectByVisibleText(species);
        } else {
            click(SPECIES_SELECT);
            shortPause();
            click(By.xpath("//mat-option//span[contains(normalize-space(),'" + species + "')]"));
        }
    }

    public void typeCity(String city) {
        type(CITY_INPUT, city);
    }

    public void clickSearch() { click(SEARCH_BUTTON); }

    public int  getPetCardCount()   { return findAll(PET_CARDS).size(); }
    public boolean hasAvailableChip() { return isPresent(AVAILABLE_CHIP); }
    public boolean isNoResultsVisible() { return isPresent(NO_RESULTS); }
    public boolean isMyApplicationsBtnVisible() { return isPresent(MY_APPS_BUTTON); }

    public void openFirstPet() {
        if (findAll(PET_CARDS).isEmpty()) return;
        findAll(PET_CARDS).get(0).click();
    }
}
