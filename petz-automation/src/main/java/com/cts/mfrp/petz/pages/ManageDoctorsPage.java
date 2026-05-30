package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * Hospital Manage Doctors at /hospital/doctors.
 *
 * Add Doctor form is hidden by default - click the "Add Doctor" header button to show it.
 * Form uses [(ngModel)] - no formControlName attributes; selectors target by placeholder.
 * Required field for save: name. Schedule defaults are fine for the happy path.
 *
 * Note: the live form has NO phone field. Doctor entity here = name + specialization
 * + schedule start/end + slot duration.
 */
public class ManageDoctorsPage extends BasePage {

    private static final By ADD_DOCTOR_BTN = By.xpath("//button[contains(normalize-space(),'Add Doctor')]");
    private static final By FULL_NAME      = By.cssSelector("input[placeholder='Dr. Jane Smith']");
    private static final By SPECIALIZATION = By.cssSelector("input[placeholder^='e.g. Surgery']");
    private static final By SLOT_DURATION  = By.cssSelector("input[type='number'][placeholder='30']");
    private static final By SAVE_BTN       = By.xpath("//button[contains(normalize-space(),'Save Doctor')]");
    private static final By CANCEL_BTN     = By.xpath("//button[contains(normalize-space(),'Cancel')]");

    public ManageDoctorsPage open() {
        goTo(AppConstants.HOSPITAL_DOCTORS_URL);
        return this;
    }

    public void clickAddDoctor() { click(ADD_DOCTOR_BTN); }

    public void fillFullName(String v)       { type(FULL_NAME, v); }
    public void fillSpecialization(String v) { type(SPECIALIZATION, v); }
    public void fillSlotDuration(String v)   { type(SLOT_DURATION, v); }

    public boolean isSaveDisabled() {
        String d = attributeOf(SAVE_BTN, "disabled");
        return d != null && !"false".equals(d);
    }

    public void clickSave()   { click(SAVE_BTN); }
    public void clickCancel() { click(CANCEL_BTN); }
}
