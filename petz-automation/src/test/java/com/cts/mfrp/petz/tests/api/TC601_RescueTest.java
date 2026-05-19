package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TC601_RescueTest extends BaseApiTest {

    @Test(groups = {"rescue", "api", "regression", "sanity", "positive"},
          description = "TC601.1 — USER GET /rescue/my returns own rescue reports")
    public void rescueMy_returnsList() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/rescue/my");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
    }

    @Test(groups = {"rescue", "api", "regression", "sanity", "positive"},
          description = "TC601.2 — NGO GET /rescue/ngo returns incoming reports")
    public void rescueNgo_returnsList() {
        Response r = RestAssured.given(ApiSpecs.asNgo()).when().get("/rescue/ngo");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
    }

    @Test(groups = {"rescue", "api", "regression", "negative"},
          description = "TC601.3 — USER hitting /rescue/ngo gets 403")
    public void rescueNgo_asUser_returns403() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/rescue/ngo");
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }

    @Test(groups = {"rescue", "api", "regression", "positive"},
          description = "TC601.4 — GET /rescue/{id} returns a single rescue report")
    public void rescueById_returnsOne() {
        // Pull an id from the user's own list — seed user already has at least one rescue.
        Response mine = RestAssured.given(ApiSpecs.asUser()).when().get("/rescue/my");
        Assert.assertEquals(mine.statusCode(), 200);
        Integer rescueId = mine.jsonPath().getInt("data[0].id");
        Assert.assertNotNull(rescueId, "Seed user is expected to have at least one rescue report");

        Response one = RestAssured.given(ApiSpecs.asUser())
                .pathParam("id", rescueId).when().get("/rescue/{id}");

        Assert.assertEquals(one.statusCode(), 200, "Body: " + one.asString());
        Assert.assertEquals(one.jsonPath().getInt("data.id"), rescueId.intValue());
    }

    // POST /rescue is multipart/form-data — deferred from this pass to keep the suite simple.
}
