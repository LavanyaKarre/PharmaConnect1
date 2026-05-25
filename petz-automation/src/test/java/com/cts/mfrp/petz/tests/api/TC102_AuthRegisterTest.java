package com.cts.mfrp.petz.tests.api;

import com.cts.mfrp.petz.api.clients.AuthClient;
import com.cts.mfrp.petz.base.BaseApiTest;
import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.models.auth.RegisterRequest;
import com.cts.mfrp.petz.models.testdata.ExpectedField;
import com.cts.mfrp.petz.models.testdata.RegisterCase;
import com.cts.mfrp.petz.models.testdata.RegisterCases;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test data for every method below lives in
 * {@code src/test/resources/testdata/auth-register.xml}; each method fetches
 * its own row via {@link RegisterCases#byId(String)}. Method names, count,
 * @Test groups, descriptions, priority, and {@code dependsOnMethods} are
 * preserved — only the inline literals (status codes, validation messages,
 * field overrides, the seeded-email reference) moved into the fixture.
 *
 * <p>The duplicate-email case (TC102.5) references the PET_OWNER seed by key
 * ({@code useSeededEmail=PET_OWNER}); the literal email itself stays in
 * {@link AppConstants}. The fixture never holds a credential.
 */
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

    /** Resolve the seeded-email role key against AppConstants. XML never stores the literal email. */
    private String resolveSeededEmail(String key) {
        return switch (key.toUpperCase()) {
            case "PET_OWNER" -> AppConstants.PET_OWNER_EMAIL;
            case "NGO"       -> AppConstants.NGO_EMAIL;
            case "HOSPITAL"  -> AppConstants.HOSPITAL_EMAIL;
            default -> throw new IllegalArgumentException(
                    "Unknown useSeededEmail key in auth-register.xml: " + key);
        };
    }

    /** Build the RegisterRequest the case asks for: EMPTY body, or GENERATED + optional overrides. */
    private RegisterRequest buildPayload(RegisterCase tc) {
        if ("EMPTY".equalsIgnoreCase(tc.getPayloadMode())) {
            return new RegisterRequest();
        }
        RegisterRequest body = validUser(tc.getEmailSuffix() == null ? "" : tc.getEmailSuffix());
        if (tc.getEmailOverride() != null)    body.setEmail(tc.getEmailOverride());
        if (tc.getPasswordOverride() != null) body.setPassword(tc.getPasswordOverride());
        if (tc.getUseSeededEmail() != null)   body.setEmail(resolveSeededEmail(tc.getUseSeededEmail()));
        return body;
    }

    private void assertExpectations(RegisterCase tc, Response r) {
        if (tc.getExpectedStatus() != null) {
            Assert.assertEquals(r.statusCode(), tc.getExpectedStatus().intValue(),
                    "[" + tc.getId() + "] Body: " + r.asString());
        }
        if (tc.getExpectedStatusMin() != null && tc.getExpectedStatusMax() != null) {
            int s = r.statusCode();
            Assert.assertTrue(s >= tc.getExpectedStatusMin() && s <= tc.getExpectedStatusMax(),
                    "[" + tc.getId() + "] Expected status in ["
                            + tc.getExpectedStatusMin() + ", " + tc.getExpectedStatusMax()
                            + "], got " + s + ". Body: " + r.asString());
        }
        if (tc.getExpectedSuccess() != null) {
            Assert.assertEquals(r.jsonPath().getBoolean("success"),
                    tc.getExpectedSuccess().booleanValue());
        }
        if (tc.getExpectedMessage() != null) {
            Assert.assertEquals(r.jsonPath().getString("message"), tc.getExpectedMessage());
        }
        if (tc.getExpectedFields() != null) {
            for (ExpectedField f : tc.getExpectedFields()) {
                Assert.assertEquals(r.jsonPath().getString("data." + f.getName()), f.getValue());
            }
        }
        if (tc.getExpectedFieldPresent() != null) {
            Assert.assertNotNull(r.jsonPath().getString("data." + tc.getExpectedFieldPresent()),
                    "[" + tc.getId() + "] expected data." + tc.getExpectedFieldPresent()
                            + " to be present. Body: " + r.asString());
        }
        if (Boolean.TRUE.equals(tc.getExpectedTokenPresent())) {
            Assert.assertNotNull(r.jsonPath().getString("data.token"),
                    "[" + tc.getId() + "] expected data.token to be present");
        }
        if (tc.getExpectedRole() != null) {
            Assert.assertEquals(r.jsonPath().getString("data.role"), tc.getExpectedRole());
        }
        if (tc.getExpectedIsApproved() != null) {
            Assert.assertEquals(r.jsonPath().getBoolean("data.isApproved"),
                    tc.getExpectedIsApproved().booleanValue(),
                    "USER role should be auto-approved");
        }
    }

    @Test(groups = {"authRegister", "api", "regression", "sanity", "positive"},
          description = "TC102.1 — register USER happy path returns 200 with token + isApproved=true")
    public void registerUser_happyPath() {
        RegisterCase tc = RegisterCases.byId("TC102_1");
        Response r = auth.register(buildPayload(tc));
        assertExpectations(tc, r);
    }

    @Test(groups = {"authRegister", "api", "regression", "negative"},
          description = "TC102.2 — register with empty body returns 400 with validation map")
    public void registerEmptyBody_returns400() {
        RegisterCase tc = RegisterCases.byId("TC102_2");
        Response r = auth.register(buildPayload(tc));
        assertExpectations(tc, r);
    }

    @Test(groups = {"authRegister", "api", "regression", "negative"},
          description = "TC102.3 — register with invalid email format returns 400")
    public void registerInvalidEmail_returns400() {
        RegisterCase tc = RegisterCases.byId("TC102_3");
        Response r = auth.register(buildPayload(tc));
        assertExpectations(tc, r);
    }

    @Test(groups = {"authRegister", "api", "regression", "negative"},
          description = "TC102.4 — register with short password returns 400")
    public void registerShortPassword_returns400() {
        RegisterCase tc = RegisterCases.byId("TC102_4");
        Response r = auth.register(buildPayload(tc));
        assertExpectations(tc, r);
    }

    @Test(groups = {"authRegister", "api", "regression", "negative"},
            description = "TC102.5 — register duplicate email returns 4xx",
            dependsOnMethods = "registerUser_happyPath")
    public void registerDuplicateEmail_returnsError() {
        RegisterCase tc = RegisterCases.byId("TC102_5");
        Response r = auth.register(buildPayload(tc));
        assertExpectations(tc, r);
    }
}
