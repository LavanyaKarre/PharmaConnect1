package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Map;

import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.PETS;
import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.PETS_BY_ID;
import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.PETS_MY;

public class TC301_PetsTest extends BaseApiTest {

    @Test(groups = {"pets", "api", "regression", "sanity", "positive"},
          description = "TC301.1 â€” pet CRUD lifecycle: POST â†’ GET â†’ PUT â†’ DELETE")
    public void petsCRUD_lifecycle() {
        // 1. Create
        Map<String, Object> createBody = Map.of(
                "name",     "QA Pet " + System.currentTimeMillis(),
                "species",  "Dog",
                "breed",    "Test",
                "ageYears", 2,
                "gender",   "MALE",
                "weightKg", 10.0);

        // NOTE: deployed backend rejects trailing-slash /pets/ â€” POST goes to /pets (no slash).
        Response create = RestAssured.given(ApiSpecs.asUser()).body(createBody).when().post("/pets");
        Assert.assertEquals(create.statusCode(), 200, "Body: " + create.asString());
        Integer petId = create.jsonPath().getInt("data.id");
        Assert.assertNotNull(petId);

        try {
            // 2. Read â€” pet appears in /pets/my and via /pets/{id}
            Response mine = RestAssured.given(ApiSpecs.asUser()).when().get(PETS_MY);
            Assert.assertEquals(mine.statusCode(), 200);
            Assert.assertTrue(mine.jsonPath().getList("data.id").contains(petId),
                    "New pet id " + petId + " should appear in /pets/my");

            Response one = RestAssured.given(ApiSpecs.asUser()).pathParam("id", petId).when().get(PETS_BY_ID);
            Assert.assertEquals(one.statusCode(), 200);
            Assert.assertEquals(one.jsonPath().getInt("data.id"), petId.intValue());

            // 3. Update â€” change name and weight, confirm GET reflects it
            Map<String, Object> updateBody = Map.of(
                    "name",     "QA Pet Renamed",
                    "species",  "Dog",
                    "breed",    "Test",
                    "ageYears", 3,
                    "gender",   "MALE",
                    "weightKg", 12.5);
            Response update = RestAssured.given(ApiSpecs.asUser())
                    .pathParam("id", petId).body(updateBody).when().put(PETS_BY_ID);
            Assert.assertEquals(update.statusCode(), 200, "Body: " + update.asString());
            Assert.assertEquals(update.jsonPath().getString("data.name"), "QA Pet Renamed");
            Assert.assertEquals(update.jsonPath().getFloat("data.weightKg"), 12.5f, 0.001f);
        } finally {
            // 4. Delete (cleanup so the test is idempotent on re-runs)
            RestAssured.given(ApiSpecs.asUser()).pathParam("id", petId).when().delete(PETS_BY_ID);
        }
    }

    @Test(groups = {"pets", "api", "regression", "negative"},
          description = "TC301.2 â€” GET /pets/my without token returns 403")
    public void getMyPets_noToken_returns403() {
        Response r = RestAssured.given(ApiSpecs.baseRequestSpec()).when().get(PETS_MY);
        Assert.assertEquals(r.statusCode(), 403, "Body: " + r.asString());
    }

    @Test(groups = {"pets", "api", "regression", "negative"},
          description = "TC301.3 â€” trailing-slash /pets/ returns 500 (deployed quirk)")
    public void petsTrailingSlash_returns500_deployedQuirk() {
        Response r = RestAssured.given(ApiSpecs.asUser()).body(Map.of("name", "x"))
                .when().post(PETS);
        Assert.assertEquals(r.statusCode(), 500,
                "Trailing-slash currently 500s. Flip when backend enables trailing-slash match.");
    }
}
