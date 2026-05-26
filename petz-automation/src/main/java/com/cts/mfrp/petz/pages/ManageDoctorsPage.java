package com.cts.mfrp.petz.pages;

import com.cts.mfrp.petz.base.BasePage;
import com.cts.mfrp.petz.constants.AppConstants;
import org.openqa.selenium.By;

/**
 * Hospital Manage Doctors at /hospital/doctors.
 */
public class ManageDoctorsPage extends BasePage {

    private static final By ADD_DOCTOR_BTN = By.xpath("//button[contains(normalize-space(),'Add Doctor') or contains(normalize-space(),'Add')]");
    private static final By FULL_NAME      = By.cssSelector("input[formcontrolname='fullName'], input[formcontrolname='name'], input[name='fullName']");
    private static final By SPECIALIZATION = By.cssSelector("input[formcontrolname='specialization'], input[name='specialization']");
    private static final By PHONE          = By.cssSelector("input[formcontrolname='phone'], input[type='tel']");
    private static final By SLOT_DURATION  = By.cssSelector("input[formcontrolname='slotDuration'], input[name='slotDuration']");
    private static final By SAVE_BTN       = By.xpath("//button[contains(normalize-space(),'Save Doctor') or contains(normalize-space(),'Save')]");
    private static final By CANCEL_BTN     = By.xpath("//button[contains(normalize-space(),'Cancel')]");
    private static final By DOCTOR_ROWS    = By.cssSelector(".doctor-card, tr, mat-card");
    private static final By EDIT_FIRST     = By.xpath("(//button[contains(normalize-space(),'Edit')])[1]");
    private static final By DELETE_FIRST   = By.xpath("(//button[contains(normalize-space(),'Delete')])[1]");
    private static final By DELETE_CONFIRM = By.xpath("//button[contains(normalize-space(),'Confirm') or contains(normalize-space(),'Yes') or contains(normalize-space(),'Delete')]");

    public ManageDoctorsPage open() {
        goTo(AppConstants.HOSPITAL_DOCTORS_URL);
        return this;
    }

    public void clickAddDoctor() { click(ADD_DOCTOR_BTN); }

    public void fillFullName(String v)      { type(FULL_NAME, v); }
    public void fillSpecialization(String v){ type(SPECIALIZATION, v); }
    public void fillPhone(String v)         { type(PHONE, v); }
    public void fillSlotDuration(String v)  { type(SLOT_DURATION, v); }

    public boolean isSaveDisabled() {
        String d = attributeOf(SAVE_BTN, "disabled");
        return d != null && !"false".equals(d);
    }

    public void clickSave()   { click(SAVE_BTN); }
    public void clickCancel() { click(CANCEL_BTN); }
    public int  getDoctorCount() { return findAll(DOCTOR_ROWS).size(); }
    public void clickEditFirst() { click(EDIT_FIRST); }
    public void clickDeleteFirst() { click(DELETE_FIRST); }
    public void confirmDelete()   { click(DELETE_CONFIRM); }
}
