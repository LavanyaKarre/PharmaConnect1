package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Browse adoption animals at /adoption/animals (titled "Find Your Companion" on the page).
 *
 * Clicking an animal card navigates to /adoption/animals/&lt;id&gt; (animal-detail page),
 * which renders the Apply form inline (not in a modal). Apply methods below operate on
 * that detail-page form.
 */
public class BrowseAdoptionPage extends BasePage {

    private static final By PAGE_TITLE       = By.xpath("//h1[contains(normalize-space(),'Find Your Companion')]");
    private static final By ANIMAL_CARDS     = By.cssSelector(".animal-card");
    private static final By NO_RESULTS       = By.xpath("//*[contains(normalize-space(),'No animals found') or contains(normalize-space(),'No matches')]");
    private static final By MY_APPS_BUTTON   = By.xpath("//*[self::a or self::button][contains(normalize-space(),'My Applications')]");

    // Apply form (on /adoption/animals/<id>)
    private static final By APPLY_REASON     = By.cssSelector("textarea[formcontrolname='reason']");
    private static final By APPLY_SUBMIT_BTN = By.xpath("//button[contains(normalize-space(),'Submit Application')]");

    public BrowseAdoptionPage open() {
        goTo(AppConstants.ADOPTION_ANIMALS_URL);
        return this;
    }

    public boolean isTitleVisible() { return isPresent(PAGE_TITLE); }

    public int     getPetCardCount()            { return findAll(ANIMAL_CARDS).size(); }
    public boolean isNoResultsVisible()         { return isPresent(NO_RESULTS); }
    public boolean isMyApplicationsBtnVisible() { return isPresent(MY_APPS_BUTTON); }

    /** Click the first animal card - navigates to /adoption/animals/&lt;id&gt; (detail page). */
    public void openFirstPet() {
        List<WebElement> cards = findAll(ANIMAL_CARDS);
        if (cards.isEmpty()) return;
        cards.get(0).click();
    }

    /** On the animal-detail page, fill the apply form's reason textarea. */
    public void fillApplyReason(String reason) { type(APPLY_REASON, reason); }

    /** Submit the application from the animal-detail apply form. */
    public void clickSubmitApplication() { click(APPLY_SUBMIT_BTN); }
}
