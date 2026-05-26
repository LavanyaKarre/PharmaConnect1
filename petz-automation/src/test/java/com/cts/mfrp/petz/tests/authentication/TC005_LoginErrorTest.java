package com.cts.mfrp.petz.tests.authentication;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.base.DriverFactory;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.pages.LoginPage;
import com.cts.mfrp.petz.utils.ExcelDataProvider;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

/**
 * TC005 - Login error on invalid credentials.
 * Data-driven from login-data.xlsx::login_errors - wrong pwd / unknown user / empty.
 */
public class TC005_LoginErrorTest extends BaseTest {

    @Test(dataProvider = "loginErrors",
          groups = {"auth", "regression", "negative"},
          description = "TC005 - Login rejects invalid credentials")
    public void TC005_LoginError(Map<String, String> row) {
        String caseId       = row.get("caseId");
        String email        = row.get("email");
        String password     = row.get("password");
        String expectedPath = row.get("expectedUrlContains");

        LoginPage login = new LoginPage().open();
        login.fillEmail(email);
        login.fillPassword(password);
        login.submitAndWait();

        String url = DriverFactory.getDriver().getCurrentUrl();
        boolean stillOnLogin = url.contains(expectedPath);
        boolean errorShown   = login.isErrorVisible();

        Assert.assertTrue(stillOnLogin || errorShown,
                "[" + caseId + "] expected login to be rejected but URL=" + url
                + " errorVisible=" + errorShown);
    }

    @DataProvider(name = "loginErrors")
    public Object[][] loginErrors() {
        List<Map<String, String>> rows = ExcelDataProvider.readSheet(
                AppConstants.LOGIN_DATA_XLSX, "login_errors");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) data[i][0] = rows.get(i);
        return data;
    }
}
