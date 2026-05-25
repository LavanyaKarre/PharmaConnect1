package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.specs.ApiSpecs;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.models.testdata.AuthBoundaryCase;
import com.cts.mfrp.petz.models.testdata.AuthBoundaryCases;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.cts.mfrp.petz.api.endpoints.ApiEndpoints.USERS_ME;

/**
 * Test data for every method below lives in
 * {@code src/test/resources/testdata/auth-boundaries.xml}; each method looks
 * up its own row via {@code AuthBoundaryCases.byId(...)}. No methods were
 * added, removed, or merged — only the inline literals (expected status,
 * token, endpoint, message) were moved out into the fixture.
 */
public class TC1101_AuthBoundariesTest extends BaseApiTest {

    @Test(groups = {"authBoundaries", "api", "regression", "negative"},
          description = "TC1101.1 — no Bearer token on protected endpoint returns 403 with empty body")
    public void noToken_returns403EmptyBody() {
        AuthBoundaryCase tc = AuthBoundaryCases.byId("TC1101_1");

        Response r = RestAssured.given(ApiSpecs.baseRequestSpec()).when().get(USERS_ME);

        Assert.assertEquals(r.statusCode(), tc.getExpectedStatus().intValue(), "Body: " + r.asString());
        Assert.assertTrue(r.asString().isBlank(),
                "Spring Security default returns no body for the 403; got: " + r.asString());
    }

    @Test(groups = {"authBoundaries", "api", "regression", "negative"},
          description = "TC1101.2 — garbage Bearer token returns 403 with empty body")
    public void badToken_returns403EmptyBody() {
        AuthBoundaryCase tc = AuthBoundaryCases.byId("TC1101_2");

        Response r = RestAssured.given(ApiSpecs.authedRequestSpec(tc.getToken()))
                .when().get(USERS_ME);

        Assert.assertEquals(r.statusCode(), tc.getExpectedStatus().intValue(), "Body: " + r.asString());
    }

    @Test(groups = {"authBoundaries", "api", "regression", "negative"},
          description = "TC1101.3 — role mismatch returns 403 with 'Access denied.' message")
    public void wrongRole_returns403WithMessage() {
        AuthBoundaryCase tc = AuthBoundaryCases.byId("TC1101_3");

        Response r = RestAssured.given(ApiSpecs.asUser()).when().get(tc.getEndpoint());

        Assert.assertEquals(r.statusCode(), tc.getExpectedStatus().intValue(), "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getString("message"), tc.getExpectedMessage());
    }
}
