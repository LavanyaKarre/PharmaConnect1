package com.cts.mfrp.petz.tests.authentication;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.base.DriverFactory;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExcelDataProvider;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * TC004 - Login happy path for each role.
 * Data-driven from login-data.xlsx::happy_paths - PET_OWNER / NGO / HOSPITAL.
 */
public class TC004_LoginHappyPathTest extends BaseTest {

    @Test(dataProvider = "happyPaths",
          groups = {"auth", "smoke", "regression", "positive"},
          description = "TC004 - Login happy path - role-based routing")
    public void TC004_LoginHappyPath(Map<String, String> row) {
        String caseId       = row.get("caseId");
        String roleName     = row.get("role");
        String expectedPath = row.get("expectedUrl");

        new LoginPage().open().loginAs(UserRole.valueOf(roleName));

        try {
            new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(AppConstants.LOGIN_WAIT))
                    .until(d -> d.getCurrentUrl().contains(expectedPath));
        } catch (Exception ignored) { /* assertion handles message */ }

        String actual = DriverFactory.getDriver().getCurrentUrl();
        Assert.assertTrue(actual.contains(expectedPath),
                "[" + caseId + "] " + roleName + " expected to land on '" + expectedPath
                + "' but got " + actual);
    }

    @DataProvider(name = "happyPaths")
    public Object[][] happyPaths() {
        List<Map<String, String>> rows = ExcelDataProvider.readSheet(
                AppConstants.LOGIN_DATA_XLSX, "happy_paths");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) data[i][0] = rows.get(i);
        return data;
    }
}
