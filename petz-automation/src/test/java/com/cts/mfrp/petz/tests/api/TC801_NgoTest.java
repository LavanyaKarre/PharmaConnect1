package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.NGO_PUBLIC;
import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.NGO_PUBLIC_BY_ID;

public class TC801_NgoTest extends BaseApiTest {

    private static final int SEEDED_NGO_ID = 1;

    @Test(groups = {"ngo", "api", "regression", "sanity", "positive"},
          description = "TC801.1 — GET /ngo/public returns list including seeded NGO")
    public void listPublicNgos_includesSeeded() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get(NGO_PUBLIC);
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getList("data.id").contains(SEEDED_NGO_ID));
    }

    @Test(groups = {"ngo", "api", "regression", "sanity", "positive"},
          description = "TC801.2 — GET /ngo/public/{id} returns single NGO")
    public void getPublicNgoById_returnsOne() {
        Response r = RestAssured.given(ApiSpecs.asUser())
                .pathParam("id", SEEDED_NGO_ID)
                .when().get(NGO_PUBLIC_BY_ID);

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getInt("data.id"), SEEDED_NGO_ID);
        Assert.assertNotNull(r.jsonPath().getString("data.name"));
    }

    @Test(groups = {"ngo", "api", "regression", "positive"},
          description = "TC801.3 — NGO GET /ngo/profile returns own NGO profile")
    public void ngoProfile_returnsOwn() {
        Response r = RestAssured.given(ApiSpecs.asNgo()).when().get("/ngo/profile");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertNotNull(r.jsonPath().getString("data.name"));
    }

    @Test(groups = {"ngo", "api", "regression", "negative"},
          description = "TC801.4 — USER hitting /ngo/profile gets 403")
    public void ngoProfile_asUser_returns403() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/ngo/profile");
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }
}
