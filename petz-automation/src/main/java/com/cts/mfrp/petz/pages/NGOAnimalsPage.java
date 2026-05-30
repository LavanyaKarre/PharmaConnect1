package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * NGO My Animals at /ngo/animals.
 *
 * The Add Animal form uses Angular's template-driven [(ngModel)] bindings — there are no
 * formControlName attributes on the inputs. Selectors target by placeholder text instead.
 */
public class NGOAnimalsPage extends BasePage {

    private static final By ADD_ANIMAL_BTN = By.xpath("//button[contains(normalize-space(),'Add Animal')]");
    private static final By NAME_INPUT     = By.cssSelector("input[placeholder='e.g. Milo']");
    private static final By SPECIES_INPUT  = By.cssSelector("input[placeholder^='Dog, Cat']");
    private static final By BREED_INPUT    = By.cssSelector("input[placeholder='Mixed']");
    private static final By AGE_INPUT      = By.cssSelector("input[type='number'][placeholder='e.g. 6']");
    private static final By DESC_INPUT     = By.cssSelector("textarea[placeholder^='e.g. Friendly']");
    private static final By SAVE_BTN       = By.xpath("//button[contains(normalize-space(),'Save Animal')]");
    private static final By CANCEL_BTN     = By.xpath("//button[contains(normalize-space(),'Cancel')]");

    public NGOAnimalsPage open() {
        goTo(AppConstants.NGO_ANIMALS_URL);
        return this;
    }

    public void clickAddAnimal() { click(ADD_ANIMAL_BTN); }

    public void fillName(String v)        { type(NAME_INPUT, v); }
    public void fillSpecies(String v)     { type(SPECIES_INPUT, v); }
    public void fillBreed(String v)       { type(BREED_INPUT, v); }
    public void fillAge(String v)         { type(AGE_INPUT, v); }
    public void fillDescription(String v) { type(DESC_INPUT, v); }

    public boolean isSaveDisabled() {
        String d = attributeOf(SAVE_BTN, "disabled");
        return d != null && !"false".equals(d);
    }

    public void clickSave()   { click(SAVE_BTN); }
    public void clickCancel() { click(CANCEL_BTN); }
}
