package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.ADOPTION_ANIMALS;
import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.ADOPTION_ANIMALS_BY_ID;

public class TC701_AdoptionPublicTest extends BaseApiTest {

    private static final int SEEDED_ANIMAL_ID = 1;

    @Test(groups = {"adoptionPublic", "api", "regression", "sanity", "positive"},
          description = "TC701.1 — GET /adoption/animals returns list including seeded Max")
    public void listAnimals_includesSeeded() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get(ADOPTION_ANIMALS);
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getList("data.id").contains(SEEDED_ANIMAL_ID),
                "Seeded animal id=" + SEEDED_ANIMAL_ID + " should be present");
    }

    @Test(groups = {"adoptionPublic", "api", "regression", "sanity", "positive"},
          description = "TC701.2 — GET /adoption/animals/{id} returns single animal")
    public void getAnimalById_returnsOne() {
        Response r = RestAssured.given(ApiSpecs.asUser())
                .pathParam("id", SEEDED_ANIMAL_ID)
                .when().get(ADOPTION_ANIMALS_BY_ID);

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getInt("data.id"), SEEDED_ANIMAL_ID);
        Assert.assertEquals(r.jsonPath().getString("data.species"), "Dog");
    }

    @Test(groups = {"adoptionPublic", "api", "regression", "positive"},
          description = "TC701.3 — GET /adoption/my-applications returns USER's applications")
    public void myApplications_returnsList() {
        Response r = RestAssured.given(ApiSpecs.asUser()).when().get("/adoption/my-applications");
        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
    }

    @Test(groups = {"adoptionPublic", "api", "regression", "negative"},
          description = "TC701.4 — POST /adoption/apply rejects a duplicate application with 400")
    public void applyDuplicate_returns400() {
        Map<String, Object> body = Map.of(
                "animalId",     SEEDED_ANIMAL_ID,
                "reason",       "qa duplicate check",
                "experience",   "some",
                "housingType",  "APARTMENT",
                "hasOtherPets", false);

        Response r = RestAssured.given(ApiSpecs.asUser()).body(body).when().post("/adoption/apply");

        Assert.assertEquals(r.statusCode(), 400, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getString("message"),
                "You already have an active application for this animal.");
    }
}
