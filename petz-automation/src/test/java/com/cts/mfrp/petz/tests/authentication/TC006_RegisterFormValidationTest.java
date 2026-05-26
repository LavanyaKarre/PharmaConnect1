package com.cts.mfrp.petz.tests.authentication;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.pages.RegisterPage;
import com.cts.mfrp.petz.utils.ExcelDataProvider;
import com.cts.mfrp.petz.utils.RandomDataGenerator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * TC006 - Register form validation across all 6 fields.
 * Data-driven from register-data.xlsx::register_validation
 * (BVA + EP + Monkey on fullName / phone / email / password / confirmPassword / accountType).
 */
public class TC006_RegisterFormValidationTest extends BaseTest {

    @Test(dataProvider = "registerValidation",
          groups = {"auth", "regression", "bva", "ep", "monkey", "negative"},
          description = "TC006 - Register form validation across all 6 fields")
    public void TC006_RegisterFormValidation(Map<String, String> row) {
        String caseId      = row.getOrDefault("caseId", "?");
        String targetField = row.getOrDefault("targetField", "fullName");
        String value       = row.getOrDefault("value", "");
        String expected    = row.getOrDefault("expectedButtonState", "DISABLED");

        RegisterPage register = new RegisterPage().open();

        fillFieldOrDefault(register, "fullName",        targetField, value, "Test User");
        fillFieldOrDefault(register, "phone",           targetField, value, "9876543210");
        fillFieldOrDefault(register, "email",           targetField, value, RandomDataGenerator.randomEmail());
        fillFieldOrDefault(register, "password",        targetField, value, "Admin@123");
        fillFieldOrDefault(register, "confirmPassword", targetField, value, "Admin@123");
        fillFieldOrDefault(register, "accountType",     targetField, value, "Pet Owner");

        register.blurField(targetField.equalsIgnoreCase("accountType") ? "email" : targetField);

        boolean disabled         = register.isCreateDisabled();
        boolean expectedDisabled = "DISABLED".equalsIgnoreCase(expected);

        Assert.assertEquals(disabled, expectedDisabled,
                "[" + caseId + "] Create Account expected " + expected
                + " but was " + (disabled ? "DISABLED" : "ENABLED"));
    }

    private void fillFieldOrDefault(RegisterPage register, String fieldName,
                                    String targetField, String testValue, String defaultValue) {
        try {
            String v = fieldName.equalsIgnoreCase(targetField) ? testValue : defaultValue;
            register.fillField(fieldName, v);
        } catch (Exception ignored) {
            // form may not expose every field — best effort
        }
    }

    @DataProvider(name = "registerValidation")
    public Object[][] registerValidation() {
        List<Map<String, String>> rows = ExcelDataProvider.readSheet(
                AppConstants.REGISTER_DATA_XLSX, "register_validation");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) data[i][0] = rows.get(i);
        return data;
    }
}
