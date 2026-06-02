package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * Pet management at /pets.
 */
public class PetManagementPage extends BasePage {

    private static final By PAGE_TITLE   = By.xpath("//*[self::h1 or self::h2][contains(normalize-space(),'My Pets') or contains(normalize-space(),'Pets')]");
    private static final By ADD_PET_BTN  = By.xpath("//button[contains(normalize-space(),'Add Pet') or contains(normalize-space(),'Add Your First Pet')]");
    private static final By NAME_INPUT   = By.cssSelector("input[formcontrolname='name']");
    private static final By SPECIES_SEL  = By.cssSelector("mat-select[formcontrolname='species']");
    private static final By AGE_INPUT    = By.cssSelector("input[formcontrolname='ageYears']");
    private static final By DESC_INPUT   = By.cssSelector("textarea[formcontrolname='notes']");
    // Submit button on /pets/new shows "Add Pet" (or "Update Pet" in edit). type=submit narrows it
    // so the header "Back" + cancel buttons can't match.
    private static final By SAVE_BTN     = By.cssSelector("button[type='submit']");
    private static final By PET_CARDS    = By.cssSelector(".pet-card, [class*='pet-card'], mat-card");
    private static final By EDIT_BTN     = By.xpath("(//button[contains(normalize-space(),'Edit')])[1]");
    private static final By DELETE_BTN   = By.xpath("(//button[contains(normalize-space(),'Delete') or contains(normalize-space(),'Remove')])[1]");

    public PetManagementPage open() {
        goTo(AppConstants.PETS_URL);
        return this;
    }

    public boolean isTitleVisible() { return isPresent(PAGE_TITLE); }
    public boolean isAddPetVisible(){ return isPresent(ADD_PET_BTN); }

    public void clickAddPet() { click(ADD_PET_BTN); }

    public void fillName(String v)        { type(NAME_INPUT, v); }
    public void fillAge(String v)         { type(AGE_INPUT, v); }
    public void fillDescription(String v) { type(DESC_INPUT, v); }

    public boolean isSaveDisabled() {
        String d = attributeOf(SAVE_BTN, "disabled");
        return d != null && !"false".equals(d);
    }

    public void clickSave() { click(SAVE_BTN); }
    public int getPetCount() { return findAll(PET_CARDS).size(); }
    public void clickFirstEdit()   { click(EDIT_BTN); }
    public void clickFirstDelete() { click(DELETE_BTN); }
}
