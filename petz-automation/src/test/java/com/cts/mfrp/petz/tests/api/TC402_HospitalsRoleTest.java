package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class TC402_HospitalsRoleTest extends BaseApiTest {

    @Test(groups = {"hospitalsRole", "api", "regression", "sanity", "positive"},
          description = "TC402.1 — HOSPITAL role gets its own profile")
    public void hospitalProfile_returnsOwn() {
        Response r = RestAssured.given(ApiSpecs.asHospital()).when().get("/hospitals/profile");

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertNotNull(r.jsonPath().getString("data.name"));
        Assert.assertEquals(r.jsonPath().getString("data.email"), "hospital@petz.com");
    }

    @Test(groups = {"hospitalsRole", "api", "regression", "negative"},
          description = "TC402.2 — USER calling /hospitals/profile gets 403 Access denied")
    public void hospitalProfile_asUser_returns403() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/hospitals/profile");

        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getString("message"), "Access denied.");
    }

    @Test(groups = {"hospitalsRole", "api", "regression", "positive"},
          description = "TC402.3 — doctor lifecycle: HOSPITAL POSTs, PUTs, then DELETEs a doctor")
    public void doctorCRUD_lifecycle() {
        // 1. Create
        long ts = System.currentTimeMillis();
        Map<String, Object> createBody = Map.of(
                "name",           "Dr QA " + ts,
                "specialization", "General",
                "phone",          "9000000001",
                "email",          "qa_" + ts + "@hosp.com",
                "scheduleStart",  "09:00",
                "scheduleEnd",    "17:00",
                "slotDuration",   30);

        Response create = RestAssured.given(ApiSpecs.asHospital())
                .body(createBody).when().post("/hospitals/profile/doctors");
        Assert.assertEquals(create.statusCode(), 200, "Body: " + create.asString());
        Integer doctorId = create.jsonPath().getInt("data.id");
        Assert.assertNotNull(doctorId);

        try {
            // 2. Update
            Map<String, Object> updateBody = Map.of(
                    "name",           "Dr QA Renamed",
                    "specialization", "Dental",
                    "phone",          "9000000002",
                    "email",          "qa_" + ts + "@hosp.com",
                    "scheduleStart",  "10:00",
                    "scheduleEnd",    "18:00",
                    "slotDuration",   30);
            Response update = RestAssured.given(ApiSpecs.asHospital())
                    .pathParam("id", doctorId).body(updateBody)
                    .when().put("/hospitals/profile/doctors/{id}");
            Assert.assertEquals(update.statusCode(), 200, "Body: " + update.asString());
            Assert.assertEquals(update.jsonPath().getString("data.name"), "Dr QA Renamed");
            Assert.assertEquals(update.jsonPath().getString("data.specialization"), "Dental");
        } finally {
            // 3. Delete (cleanup)
            RestAssured.given(ApiSpecs.asHospital())
                    .pathParam("id", doctorId)
                    .when().delete("/hospitals/profile/doctors/{id}");
        }
    }
}
