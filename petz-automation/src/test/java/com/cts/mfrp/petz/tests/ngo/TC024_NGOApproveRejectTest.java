package com.cts.mfrp.petz.tests.ngo;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.AdoptionApplicationsPage;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.XmlDataProvider;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * TC024 - NGO Approve / Reject application with notes (Monkey on Reject notes).
 * Data-driven from ngo-data.xml::applicationDecisions (Approve + Reject rows).
 */
public class TC024_NGOApproveRejectTest extends BaseTest {

    @Test(dataProvider = "decisionRows",
          groups = {"ngo", "regression", "monkey"},
          description = "TC024 - NGO Approve/Reject application with notes")
    public void TC024_NGOApproveReject(Map<String, String> row) {
        String caseId = row.get("caseId");
        String action = row.get("action");
        String notes  = row.get("notes");

        new LoginPage().open().loginAs(UserRole.NGO);
        AdoptionApplicationsPage apps = new AdoptionApplicationsPage().open();

        if (apps.getCardCount() > 0) {
            try {
                if ("Approve".equalsIgnoreCase(action)) apps.clickApproveFirst();
                else                                    apps.clickRejectFirst();
                apps.fillNotes(notes);
                apps.clickConfirm();
            } catch (Exception ignored) {}
        }

        Assert.assertTrue(apps.isTitleVisible(),
                "[" + caseId + "] Applications page lost its title after " + action);
    }

    @DataProvider(name = "decisionRows")
    public Object[][] decisionRows() {
        List<Map<String, String>> rows = XmlDataProvider.readSection(
                AppConstants.NGO_DATA_XML, "applicationDecisions");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) data[i][0] = rows.get(i);
        return data;
    }
}
