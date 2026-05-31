package com.cts.mfrp.petz.cucumber.steps;

import com.cts.mfrp.petz.constants.UserRole;
import com.cts.mfrp.petz.pages.LoginPage;
import io.cucumber.java.en.Given;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Login steps. Because the browser session is shared across the whole run (see Hooks),
 * we track the currently-logged-in role in a static field and only re-login when the
 * role actually changes. That gives "one login per journey" — login fires once at the
 * top of the pet-owner feature, once for ngo, once for hospital — matching the TestNG
 * suite's @BeforeClass behaviour, while staying idiomatic BDD (a Background step).
 */
public class AuthSteps {

    private static final Logger logger = LoggerFactory.getLogger(AuthSteps.class);

    /** Role currently authenticated in the shared browser session, or null if none. */
    private static UserRole loggedInRole;

    @Given("I am logged in as a pet owner")
    public void loggedInAsPetOwner() {
        ensureLoggedInAs(UserRole.PET_OWNER);
    }

    @Given("I am logged in as an NGO")
    public void loggedInAsNgo() {
        ensureLoggedInAs(UserRole.NGO);
    }

    @Given("I am logged in as a hospital")
    public void loggedInAsHospital() {
        ensureLoggedInAs(UserRole.HOSPITAL);
    }

    private void ensureLoggedInAs(UserRole role) {
        if (role == loggedInRole) {
            logger.info("Already logged in as {} - reusing session", role);
            return;
        }
        new LoginPage().open().loginAs(role);
        loggedInRole = role;
        logger.info("Logged in as {}", role);
    }
}
