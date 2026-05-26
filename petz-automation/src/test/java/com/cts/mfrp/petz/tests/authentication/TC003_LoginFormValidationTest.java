package com.cts.mfrp.petz.tests.authentication;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExcelDataProvider;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * TC003 - Login form validation (BVA + EP + Monkey on email and password).
 * Data-driven from login-data.xlsx::login_validation - 12 rows mixing all 3 dimensions.
 */
public class TC003_LoginFormValidationTest extends BaseTest {

    @Test(dataProvider = "loginValidation",
          groups = {"auth", "regression", "bva", "ep", "monkey", "negative"},
          description = "TC003 - Login form validation (BVA + EP + Monkey on email and password)")
    public void TC003_LoginFormValidation(Map<String, String> row) {
        String caseId      = row.getOrDefault("caseId", "?");
        String field       = row.getOrDefault("field", "email");
        String value       = row.getOrDefault("value", "");
        boolean otherValid = "YES".equalsIgnoreCase(row.getOrDefault("otherFieldValid", "YES"));
        String expected    = row.getOrDefault("expectedButtonState", "DISABLED");

        LoginPage login = new LoginPage().open();

        if ("email".equalsIgnoreCase(field)) {
            login.fillEmail(value);
            if (otherValid) login.fillPassword(AppConstants.PET_OWNER_PASSWORD);
            login.blurEmail();
        } else {
            if (otherValid) login.fillEmail(AppConstants.PET_OWNER_EMAIL);
            login.fillPassword(value);
            login.blurPassword();
        }

        boolean disabled         = login.isSignInDisabled();
        boolean expectedDisabled = "DISABLED".equalsIgnoreCase(expected);

        Assert.assertEquals(disabled, expectedDisabled,
                "[" + caseId + "] Sign In expected " + expected
                + " but was " + (disabled ? "DISABLED" : "ENABLED"));
    }

    @DataProvider(name = "loginValidation")
    public Object[][] loginValidation() {
        List<Map<String, String>> rows = ExcelDataProvider.readSheet(
                AppConstants.LOGIN_DATA_XLSX, "login_validation");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) data[i][0] = rows.get(i);
        return data;
    }
}
