package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class TC501_AppointmentsTest extends BaseApiTest {

    @Test(groups = {"appointments", "api", "regression", "sanity", "positive"},
          description = "TC501.1 — USER GET /appointments/my returns list")
    public void appointmentsMy_returnsList() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/appointments/my");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
        Assert.assertNotNull(r.jsonPath().getList("data"));
    }

    @Test(groups = {"appointments", "api", "regression", "sanity", "positive"},
          description = "TC501.2 — GET /appointments/slots with doctorId and date returns slot list")
    public void appointmentsSlots_returnsSlots() {
        Response r = RestAssured.given(ApiSpecs.asUser())
                .queryParam("doctorId", 1)
                .queryParam("date", "2026-06-15")
                .when().get("/appointments/slots");

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertFalse(r.jsonPath().getList("data").isEmpty(), "Expected at least one slot");
    }

    @Test(groups = {"appointments", "api", "regression", "positive"},
          description = "TC501.3 — HOSPITAL GET /appointments/hospital returns list")
    public void appointmentsHospital_returnsList() {
        Response r = RestAssured.given(ApiSpecs.asHospital()).when().get("/appointments/hospital");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
    }

    @Test(groups = {"appointments", "api", "regression", "negative"},
          description = "TC501.4 — USER hitting /appointments/hospital gets 403")
    public void appointmentsHospital_asUser_returns403() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/appointments/hospital");
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }

    @Test(groups = {"appointments", "api", "regression", "positive"},
          description = "TC501.5 — book an appointment and then cancel it")
    public void bookAndCancelAppointment_lifecycle() {
        // Pick the user's first pet (seed: Bruno = id 1, but fetch at runtime to stay robust)
        Response pets = RestAssured.given(ApiSpecs.asUser()).when().get("/pets/my");
        Integer petId = pets.jsonPath().getInt("data[0].id");
        Assert.assertNotNull(petId, "Seed user must own at least one pet for this test");

        Map<String, Object> body = Map.of(
                "hospitalId", 1,
                "doctorId",   1,
                "petId",      petId,
                "apptDate",   "2027-12-31",   // far future to avoid slot collision on reruns
                "apptTime",   "10:00",
                "reason",     "QA suite — please cancel");

        Response book = RestAssured.given(ApiSpecs.asUser()).body(body).when().post("/appointments");
        Assert.assertEquals(book.statusCode(), 200, "Body: " + book.asString());
        Integer apptId = book.jsonPath().getInt("data.id");
        Assert.assertNotNull(apptId);

        try {
            // Appears in /appointments/my
            Response mine = RestAssured.given(ApiSpecs.asUser()).when().get("/appointments/my");
            Assert.assertTrue(mine.jsonPath().getList("data.id").contains(apptId),
                    "Newly booked appointment should appear in /appointments/my");
        } finally {
            Response cancel = RestAssured.given(ApiSpecs.asUser())
                    .pathParam("id", apptId).when().delete("/appointments/{id}");
            Assert.assertEquals(cancel.statusCode(), 200,
                    "DELETE cleanup failed. Body: " + cancel.asString());
        }
    }
}
