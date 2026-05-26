package com.cts.mfrp.petz.tests.landing;

import com.cts.mfrp.petz.base.BaseTest;
import com.cts.mfrp.petz.pages.LandingPage;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * TC001 - Landing page renders all key sections.
 * Public, unauthenticated. Verifies the hero / stats / features / footer are visible.
 */
public class TC001_LandingRendersTest extends BaseTest {

    @Test(groups = {"landing", "smoke", "regression", "positive"},
          description = "TC001 - Landing page renders all key sections")
    public void TC001_LandingRenders() {
        LandingPage landing = new LandingPage().open();

        Assert.assertTrue(landing.isLogoVisible(),     "Landing logo not visible");
        Assert.assertTrue(landing.isHeroVisible(),     "Hero section not visible");
        Assert.assertTrue(landing.isStatsVisible(),    "Stats strip not visible");
        Assert.assertTrue(landing.isFeaturesVisible(), "Feature cards not visible");
        Assert.assertTrue(landing.isFooterVisible(),   "Footer not visible");
    }
}
