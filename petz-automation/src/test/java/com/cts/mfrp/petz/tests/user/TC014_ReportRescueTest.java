package com.cts.mfrp.petz.tests.user;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.pages.ReportRescuePage;
import com.cts.mfrp.petz.utils.XmlDataProvider;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * TC014 - Report Rescue - location + description validation (incl. Monkey on condition).
 * Data-driven from user-data.xml::rescues.
 */
public class TC014_ReportRescueTest extends BaseTest {

    @Test(dataProvider = "rescueRows",
          groups = {"user", "regression", "bva", "monkey"},
          description = "TC014 - Report Rescue - landmark / condition / phone validation")
    public void TC014_ReportRescue(Map<String, String> row) {
        String caseId   = row.getOrDefault("caseId", "?");
        String field    = row.getOrDefault("field", "landmark");
        String value    = row.getOrDefault("value", "");
        String expected = row.getOrDefault("expected", "DISABLED");

        new LoginPage().open().loginAs(UserRole.PET_OWNER);
        ReportRescuePage report = new ReportRescuePage().open();

        try {
            report.selectAnimalType("dog");
            report.selectUrgency("medium");
            report.selectArea("Anna Nagar");

            if ("landmark".equalsIgnoreCase(field))           { report.fillLandmark(value);    report.fillCondition("Limping"); report.fillReporterPhone("9876543210"); }
            else if ("condition".equalsIgnoreCase(field))     { report.fillLandmark("Near park"); report.fillCondition(value);    report.fillReporterPhone("9876543210"); }
            else if ("reporterPhone".equalsIgnoreCase(field)) { report.fillLandmark("Near park"); report.fillCondition("Limping"); report.fillReporterPhone(value); }
            else                                              { report.fillLandmark("Near park"); report.fillCondition("Limping"); report.fillReporterPhone("9876543210"); }
        } catch (Exception ignored) {}

        boolean disabled         = report.isSubmitDisabled();
        boolean expectedDisabled = "DISABLED".equalsIgnoreCase(expected);
        Assert.assertEquals(disabled, expectedDisabled,
                "[" + caseId + "] Submit Rescue expected " + expected
                + " but was " + (disabled ? "DISABLED" : "ENABLED"));
    }

    @DataProvider(name = "rescueRows")
    public Object[][] rescueRows() {
        List<Map<String, String>> rows = XmlDataProvider.readSection(
                AppConstants.USER_DATA_XML, "rescues");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) data[i][0] = rows.get(i);
        return data;
    }
}
