package com.cts.mfrp.petz.tests.landing;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.base.DriverFactory;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.pages.LandingPage;
import com.cts.mfrp.petz.utils.XmlDataProvider;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * TC002 - Landing page CTAs route to the right URL.
 * Data-driven from landing-data.xml::landingCtas.
 */
public class TC002_LandingNavigationTest extends BaseTest {

    @Test(dataProvider = "landingCtas",
          groups = {"landing", "regression", "positive"},
          description = "TC002 - Landing navigation routes correctly")
    public void TC002_LandingNavigation(Map<String, String> row) {
        String cta          = row.get("cta");
        String expectedPath = row.get("expectedPath");

        new LandingPage().open().clickByLabel(cta);

        try {
            new WebDriverWait(DriverFactory.getDriver(), Duration.ofSeconds(10))
                    .until(d -> d.getCurrentUrl().contains(expectedPath));
        } catch (Exception ignored) { /* falls through to assertion message */ }

        String actual = DriverFactory.getDriver().getCurrentUrl();
        Assert.assertTrue(actual.contains(expectedPath),
                "CTA '" + cta + "': expected URL to contain '" + expectedPath + "' but got " + actual);
    }

    @DataProvider(name = "landingCtas")
    public Object[][] landingCtas() {
        List<Map<String, String>> rows = XmlDataProvider.readSection(
                AppConstants.LANDING_DATA_XML, "landingCtas");
        Object[][] data = new Object[rows.size()][1];
        for (int i = 0; i < rows.size(); i++) data[i][0] = rows.get(i);
        return data;
    }
}
