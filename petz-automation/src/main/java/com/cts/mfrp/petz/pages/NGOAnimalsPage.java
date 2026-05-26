package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * NGO My Animals at /ngo/animals.
 * NOTE: filter is a native <select>, not mat-select.
 */
public class NGOAnimalsPage extends BasePage {

    private static final By ADD_ANIMAL_BTN = By.xpath("//button[contains(normalize-space(),'Add Animal') or contains(normalize-space(),'Add')]");
    private static final By NAME_INPUT     = By.cssSelector("input[formcontrolname='name'], input[name='name']");
    private static final By SPECIES_SELECT = By.cssSelector("select[formcontrolname='species'], mat-select[formcontrolname='species']");
    private static final By AGE_INPUT      = By.cssSelector("input[formcontrolname='age'], input[type='number']");
    private static final By DESC_INPUT     = By.cssSelector("textarea[formcontrolname='description']");
    private static final By SAVE_BTN       = By.xpath("//button[contains(normalize-space(),'Save') or contains(normalize-space(),'Add') or contains(normalize-space(),'Create')]");
    private static final By CANCEL_BTN     = By.xpath("//button[contains(normalize-space(),'Cancel')]");
    private static final By ANIMAL_CARDS   = By.cssSelector(".animal-card, mat-card");

    public NGOAnimalsPage open() {
        goTo(AppConstants.NGO_ANIMALS_URL);
        return this;
    }

    public void clickAddAnimal() { click(ADD_ANIMAL_BTN); }

    public void fillName(String v)        { type(NAME_INPUT, v); }
    public void fillAge(String v)         { type(AGE_INPUT, v); }
    public void fillDescription(String v) { type(DESC_INPUT, v); }

    public void selectSpecies(String species) {
        if (!isPresent(SPECIES_SELECT)) return;
        WebElement el = find(SPECIES_SELECT);
        if ("select".equalsIgnoreCase(el.getTagName())) {
            new Select(el).selectByVisibleText(species);
        } else {
            click(SPECIES_SELECT);
            shortPause();
            click(By.xpath("//mat-option//span[contains(normalize-space(),'" + species + "')]"));
        }
    }

    public boolean isSaveDisabled() {
        String d = attributeOf(SAVE_BTN, "disabled");
        return d != null && !"false".equals(d);
    }

    public void clickSave()   { click(SAVE_BTN); }
    public void clickCancel() { click(CANCEL_BTN); }
    public int  getAnimalCount() { return findAll(ANIMAL_CARDS).size(); }
}
