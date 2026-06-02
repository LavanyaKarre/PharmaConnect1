package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Report a rescue at /rescue/report.
 */
public class ReportRescuePage extends BasePage {

    private static final By ANIMAL_TYPE   = By.cssSelector("select[formcontrolname='animalType'], mat-select[formcontrolname='animalType']");
    private static final By URGENCY       = By.cssSelector("select[formcontrolname='urgency'], mat-select[formcontrolname='urgency']");
    private static final By AREA          = By.cssSelector("select[formcontrolname='area'], mat-select[formcontrolname='area']");
    private static final By LANDMARK      = By.cssSelector("input[formcontrolname='landmark'], input[name='landmark']");
    private static final By CONDITION     = By.cssSelector("textarea[formcontrolname='condition'], textarea[name='condition'], textarea[formcontrolname='description']");
    private static final By REPORTER_PHONE = By.cssSelector("input[formcontrolname='reporterPhone'], input[name='reporterPhone']");
    private static final By SUBMIT_BTN    = By.xpath("//button[contains(normalize-space(),'Submit') or contains(normalize-space(),'Report')]");

    public ReportRescuePage open() {
        goTo(AppConstants.RESCUE_REPORT_URL);
        return this;
    }

    public void selectAnimalType(String value) { selectAuto(ANIMAL_TYPE, value); }
    public void selectUrgency(String value)    { selectAuto(URGENCY, value); }
    public void selectArea(String value)       { selectAuto(AREA, value); }

    public void fillLandmark(String v)      { type(LANDMARK, v); }
    public void fillCondition(String v)     { type(CONDITION, v); }
    public void fillReporterPhone(String v) { type(REPORTER_PHONE, v); }

    public boolean isSubmitDisabled() {
        String d = attributeOf(SUBMIT_BTN, "disabled");
        return d != null && !"false".equals(d);
    }

    public void clickSubmit() { click(SUBMIT_BTN); }

    private void selectAuto(By locator, String value) {
        if (!isPresent(locator)) return;
        WebElement el = find(locator);
        if ("select".equalsIgnoreCase(el.getTagName())) {
            new Select(el).selectByVisibleText(value);
        } else {
            click(locator);
            shortPause();
            click(By.xpath("//mat-option//span[contains(normalize-space(),'" + value + "')]"));
        }
    }
}
