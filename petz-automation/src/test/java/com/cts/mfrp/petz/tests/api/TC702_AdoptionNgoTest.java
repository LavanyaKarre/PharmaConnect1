package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

public class TC702_AdoptionNgoTest extends BaseApiTest {

    @Test(groups = {"adoptionNgo", "api", "regression", "sanity", "positive"},
          description = "TC702.1 — NGO GET /adoption/ngo/animals returns animals owned by NGO")
    public void ngoAnimals_returnsList() {
        Response r = RestAssured.given(ApiSpecs.asNgo()).when().get("/adoption/ngo/animals");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
    }

    @Test(groups = {"adoptionNgo", "api", "regression", "sanity", "positive"},
          description = "TC702.2 — NGO GET /adoption/ngo/applications returns applications")
    public void ngoApplications_returnsList() {
        Response r = RestAssured.given(ApiSpecs.asNgo()).when().get("/adoption/ngo/applications");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
    }

    @Test(groups = {"adoptionNgo", "api", "regression", "negative"},
          description = "TC702.3 — USER hitting /adoption/ngo/animals gets 403")
    public void ngoAnimals_asUser_returns403() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/adoption/ngo/animals");
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }

    @Test(groups = {"adoptionNgo", "api", "regression", "positive"},
          description = "TC702.4 — NGO animal lifecycle: POST → PUT → DELETE")
    public void ngoAnimalCRUD_lifecycle() {
        // 1. Create — backend defaults the optional fields, so a minimal body works
        Map<String, Object> createBody = Map.of(
                "name",    "QA Animal " + System.currentTimeMillis(),
                "species", "Dog",
                "city",    "Chennai");
        Response create = RestAssured.given(ApiSpecs.asNgo())
                .body(createBody).when().post("/adoption/ngo/animals");
        Assert.assertEquals(create.statusCode(), 200, "Body: " + create.asString());
        Integer animalId = create.jsonPath().getInt("data.id");
        Assert.assertNotNull(animalId);

        try {
            // 2. Update
            Map<String, Object> updateBody = Map.of(
                    "name",         "QA Animal Renamed",
                    "species",      "Dog",
                    "breed",        "Indie Mix",
                    "city",         "Chennai",
                    "isVaccinated", true,
                    "status",       "AVAILABLE");
            Response update = RestAssured.given(ApiSpecs.asNgo())
                    .pathParam("id", animalId).body(updateBody)
                    .when().put("/adoption/ngo/animals/{id}");
            Assert.assertEquals(update.statusCode(), 200, "Body: " + update.asString());
            Assert.assertEquals(update.jsonPath().getString("data.name"), "QA Animal Renamed");
            Assert.assertTrue(update.jsonPath().getBoolean("data.isVaccinated"));
        } finally {
            // 3. Delete (cleanup)
            RestAssured.given(ApiSpecs.asNgo())
                    .pathParam("id", animalId)
                    .when().delete("/adoption/ngo/animals/{id}");
        }
    }
}
