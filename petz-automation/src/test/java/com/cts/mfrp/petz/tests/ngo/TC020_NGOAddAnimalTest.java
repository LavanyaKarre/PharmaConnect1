package com.cts.mfrp.petz.tests.ngo;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.NGOAnimalsPage;
import com.cts.mfrp.petz.utils.XmlDataProvider;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * TC020 - NGO Add Animal validation.
 * Data-driven from ngo-data.xml::animals (BVA on name/age, Monkey on description).
 */
public class TC020_NGOAddAnimalTest extends BaseTest {

    @Test(dataProvider = "animalRows",
          groups = {"ngo", "regression", "bva", "monkey"},
          description = "TC020 - NGO Add Animal validation - name / age / description")
    public void TC020_NGOAddAnimal(Map<String, String> row) {
        String caseId   = row.getOrDefault("caseId", "?");
        String field    = row.getOrDefault("field", "name");
        String value    = row.getOrDefault("value", "");
        String expected = row.getOrDefault("expected", "DISABLED");

        new LoginPage().open().loginAs(UserRole.NGO);
        NGOAnimalsPage animals = new NGOAnimalsPage().open();
        try { animals.clickAddAnimal(); } catch (Exception ignored) {}

        try {
            if ("name".equalsIgnoreCase(field))             { animals.fillName(value);   animals.fillAge("3");   animals.fillDescription("Healthy and friendly"); }
            else if ("age".equalsIgnoreCase(field))         { animals.fillName("Rex");   animals.fillAge(value); animals.fillDescription("Healthy and friendly"); }
            else if ("description".equalsIgnoreCase(field)) { animals.fillName("Rex");   animals.fillAge("3");   animals.fillDescription(value); }
            else                                            { animals.fillName("Rex");   animals.fillAge("3");   animals.fillDescription("Default"); }
            try { animals.selectSpecies("Dog"); } catch (Exception ignored) {}
        } catch (Exception ignored) {}

        boolean disabled         = animals.isSaveDisabled();
        boolean expectedDisabled = "DISABLED".equalsIgnoreCase(expected);
        Assert.assertEquals(disabled, expectedDisabled,
                "[" + caseId + "] Save Animal expected " + expected
                + " but was " + (disabled ? "DISABLED" : "ENABLED"));
    }

    @DataProvider(name = "animalRows")
    public Object[][] animalRows() {
        List<Map<String, String>> rows = XmlDataProvider.readSection(AppConstants.NGO_DATA_XML, "animals");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) data[i][0] = rows.get(i);
        return data;
    }
}
