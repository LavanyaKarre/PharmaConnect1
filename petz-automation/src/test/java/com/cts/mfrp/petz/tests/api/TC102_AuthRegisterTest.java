package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.clients.AuthClient;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.models.auth.RegisterRequest;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TC102_AuthRegisterTest extends BaseApiTest {

    private AuthClient auth;

    @BeforeClass(alwaysRun = true)
    public void init() {
        auth = new AuthClient();
    }

    private RegisterRequest validUser(String emailSuffix) {
        long ts = System.currentTimeMillis();
        return new RegisterRequest(
                "Auto User " + ts,
                "auto_" + ts + emailSuffix + "@example.com",
                "Strong@123",
                "98765" + String.format("%05d", (int) (Math.random() * 100_000)),
                "USER");
    }

    @Test(groups = {"authRegister", "api", "regression", "sanity", "positive"},
          description = "TC102.1 — register USER happy path returns 200 with token + isApproved=true")
    public void registerUser_happyPath() {
        Response r = auth.register(validUser("a"));

        Assert.assertEquals(r.statusCode(), 200, "Body: " + r.asString());
        Assert.assertTrue(r.jsonPath().getBoolean("success"));
        Assert.assertNotNull(r.jsonPath().getString("data.token"));
        Assert.assertEquals(r.jsonPath().getString("data.role"), "USER");
        Assert.assertEquals(r.jsonPath().getBoolean("data.isApproved"), true,
                "USER role should be auto-approved");
    }

    @Test(groups = {"authRegister", "api", "regression", "negative"},
          description = "TC102.2 — register with empty body returns 400 with validation map")
    public void registerEmptyBody_returns400() {
        Response r = auth.register(new RegisterRequest());

        Assert.assertEquals(r.statusCode(), 400, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getString("message"), "Validation failed.");
        Assert.assertEquals(r.jsonPath().getString("data.name"),     "must not be blank");
        Assert.assertEquals(r.jsonPath().getString("data.email"),    "must not be blank");
        Assert.assertEquals(r.jsonPath().getString("data.password"), "must not be blank");
    }

    @Test(groups = {"authRegister", "api", "regression", "negative"},
          description = "TC102.3 — register with invalid email format returns 400")
    public void registerInvalidEmail_returns400() {
        RegisterRequest body = validUser("b");
        body.setEmail("not-an-email");
        Response r = auth.register(body);

        Assert.assertEquals(r.statusCode(), 400, "Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getString("data.email"),
                "must be a well-formed email address");
    }

    @Test(groups = {"authRegister", "api", "regression", "negative"},
          description = "TC102.4 — register with short password returns 400")
    public void registerShortPassword_returns400() {
        RegisterRequest body = validUser("c");
        body.setPassword("ab");
        Response r = auth.register(body);

        Assert.assertEquals(r.statusCode(), 400, "Body: " + r.asString());
        Assert.assertNotNull(r.jsonPath().getString("data.password"));
    }

    @Test(groups = {"authRegister", "api", "regression", "negative"},
            description = "TC102.5 — register duplicate email returns 4xx",
            dependsOnMethods = "registerUser_happyPath")
    public void registerDuplicateEmail_returnsError() {
        RegisterRequest body = validUser("d");
        body.setEmail("user@petz.com");
        Response r = auth.register(body);

        Assert.assertTrue(r.statusCode() >= 400 && r.statusCode() < 500,
                "Expected 4xx for duplicate, got " + r.statusCode() + ". Body: " + r.asString());
        Assert.assertEquals(r.jsonPath().getBoolean("success"), false);
    }
}
