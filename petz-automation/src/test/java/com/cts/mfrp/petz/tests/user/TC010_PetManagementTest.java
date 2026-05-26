package com.cts.mfrp.petz.tests.user;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.PetManagementPage;
import com.cts.mfrp.petz.utils.XmlDataProvider;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * TC010 - Pet Management add form validation.
 * Data-driven from user-data.xml::pets (BVA on name/age, Monkey on description).
 */
public class TC010_PetManagementTest extends BaseTest {

    @Test(dataProvider = "petRows",
          groups = {"user", "regression", "bva", "monkey"},
          description = "TC010 - Pet Management add - validation across name / age / description")
    public void TC010_PetManagement(Map<String, String> row) {
        String caseId   = row.getOrDefault("caseId", "?");
        String field    = row.getOrDefault("field", "name");
        String value    = row.getOrDefault("value", "");
        String expected = row.getOrDefault("expected", "DISABLED");

        new LoginPage().open().loginAs(UserRole.PET_OWNER);
        PetManagementPage pets = new PetManagementPage().open();
        if (pets.isAddPetVisible()) pets.clickAddPet();

        try {
            if ("name".equalsIgnoreCase(field))        { pets.fillName(value);   pets.fillAge("3");   pets.fillDescription("A friendly pet"); }
            else if ("age".equalsIgnoreCase(field))    { pets.fillName("Buddy"); pets.fillAge(value); pets.fillDescription("A friendly pet"); }
            else if ("description".equalsIgnoreCase(field)) { pets.fillName("Buddy"); pets.fillAge("3"); pets.fillDescription(value); }
            else                                            { pets.fillName("Buddy"); pets.fillAge("3"); pets.fillDescription("Default"); }
        } catch (Exception ignored) { /* form may not expose every field */ }

        boolean disabled         = pets.isSaveDisabled();
        boolean expectedDisabled = "DISABLED".equalsIgnoreCase(expected);
        Assert.assertEquals(disabled, expectedDisabled,
                "[" + caseId + "] Save Pet expected " + expected
                + " but was " + (disabled ? "DISABLED" : "ENABLED"));
    }

    @DataProvider(name = "petRows")
    public Object[][] petRows() {
        List<Map<String, String>> rows = XmlDataProvider.readSection(AppConstants.USER_DATA_XML, "pets");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) data[i][0] = rows.get(i);
        return data;
    }
}
