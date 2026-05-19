package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.HOSPITALS_PUBLIC;
import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.HOSPITALS_PUBLIC_BY_ID;

public class TC401_HospitalsPublicTest extends BaseApiTest {

    private static final int SEEDED_HOSPITAL_ID = 1;

    @Test(groups = {"hospitalsPublic", "api", "regression", "sanity", "positive"},
          description = "TC401.1 â€” GET /hospitals/public returns list with seeded VetCare hospital")
    public void listPublicHospitals_returnsSeeded() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get(HOSPITALS_PUBLIC);

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getList("data.id").contains(SEEDED_HOSPITAL_ID),
                "Seeded hospital id=" + SEEDED_HOSPITAL_ID + " should be present");
    }

    @Test(groups = {"hospitalsPublic", "api", "regression", "sanity", "positive"},
          description = "TC401.2 â€” GET /hospitals/public/{id} returns single hospital")
    public void getPublicHospitalById_returnsOne() {
        Response r = RestAssured.given(ApiSpecs.asUser())
                .pathParam("id", SEEDED_HOSPITAL_ID)
                .when().get(HOSPITALS_PUBLIC_BY_ID);

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getInt("data.id"), SEEDED_HOSPITAL_ID);
        Assert.assertNotNull(r.jsonPath().getString("data.name"));
    }

    @Test(groups = {"hospitalsPublic", "api", "regression", "negative"},
          description = "TC401.3 â€” /hospitals/public needs auth despite being doc'd public")
    public void hospitalsPublic_noToken_returns403() {
        Response r = RestAssured.given(ApiSpecs.baseRequestSpec()).when().get(HOSPITALS_PUBLIC);
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }

    @Test(groups = {"hospitalsPublic", "api", "regression", "positive"},
          description = "TC401.4 â€” GET /hospitals/public/{id}/doctors returns the hospital's doctors")
    public void getHospitalDoctors_returnsList() {
        Response r = RestAssured.given(ApiSpecs.asUser())
                .pathParam("id", SEEDED_HOSPITAL_ID)
                .when().get("/hospitals/public/{id}/doctors");

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
        Assert.assertNotNull(r.jsonPath().getList("data"));
    }

    @Test(groups = {"hospitalsPublic", "api", "regression", "positive"},
          description = "TC401.5 â€” GET .../doctors/{doctorId}/slots returns available slots for the date")
    public void getDoctorSlots_returnsSlots() {
        Response r = RestAssured.given(ApiSpecs.asUser())
                .pathParam("hospitalId", SEEDED_HOSPITAL_ID)
                .pathParam("doctorId",   1)
                .queryParam("date", "2026-07-01")
                .when().get("/hospitals/public/{hospitalId}/doctors/{doctorId}/slots");

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertFalse(r.jsonPath().getList("data").isEmpty(),
                "Expected at least one slot for the future date");
    }
}
