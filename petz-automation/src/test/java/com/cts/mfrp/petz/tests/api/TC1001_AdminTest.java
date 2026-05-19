package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TC1001_AdminTest extends BaseApiTest {

    @DataProvider(name = "adminListings")
    public Object[][] adminListings() {
        return new Object[][] {
                { "/admin/users" },
                { "/admin/pending-approvals" },
                { "/admin/ngos" },
                { "/admin/ngos/unverified" },
                { "/admin/hospitals" },
                { "/admin/rescues" },
                { "/admin/adoptions" }
        };
    }

    @Test(groups = {"admin", "api", "regression", "sanity", "positive"},
            dataProvider = "adminListings",
            description = "TC1001.1 — every admin listing endpoint returns 200 with success=true")
    public void adminListing_returns200(String path) {
        Response r = RestAssured.given(ApiSpecs.asAdmin()).when().get(path);

        Assert.assertEquals(r.statusCode(), 200, path + " Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"), path + " should set success=true");
    }

    @Test(groups = {"admin", "api", "regression", "negative"},
          description = "TC1001.2 — USER hitting /admin/users gets 403 Access denied")
    public void adminUsers_asUser_returns403() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/admin/users");
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getString("message"), "Access denied.");
    }

    @Test(groups = {"admin", "api", "regression", "negative"},
          description = "TC1001.3 — /admin/users without token returns 403")
    public void adminUsers_noToken_returns403() {
        Response r = RestAssured.given(ApiSpecs.baseRequestSpec()).when().get("/admin/users");
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }
}
