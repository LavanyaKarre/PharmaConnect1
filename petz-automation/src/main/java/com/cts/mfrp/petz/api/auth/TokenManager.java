package com.cts.mfrp.petz.api.auth;

import com.cts.mfrp.petz.api.clients.AuthClient;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.Map;

/**
 * Caches a JWT per role so each test suite logs in once per seed account.
 * Seed credentials are documented in memory: reference-petz-seed-accounts.
 */
public final class TokenManager {

    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);

    private static final String PASSWORD = "admin@petz123";

    private static final Map<Role, String> EMAILS = new EnumMap<>(Role.class);
    static {
        EMAILS.put(Role.ADMIN,    "admin@petz.com");
        EMAILS.put(Role.NGO,      "ngo@petz.com");
        EMAILS.put(Role.HOSPITAL, "hospital@petz.com");
        EMAILS.put(Role.USER,     "user@petz.com");
    }

    private static final Map<Role, String> TOKENS = new EnumMap<>(Role.class);
    private static final AuthClient AUTH = new AuthClient();

    private TokenManager() {}

    public static synchronized String tokenFor(Role role) {
        return TOKENS.computeIfAbsent(role, TokenManager::login);
    }

    private static String login(Role role) {
        String email = EMAILS.get(role);
        log.debug("Logging in seed account for role {} ({})", role, email);
        Response r = AUTH.login(email, PASSWORD);
        if (r.statusCode() != 200) {
            log.error("Seed login failed for role {} ({}): HTTP {} - {}",
                    role, email, r.statusCode(), r.asString());
            throw new IllegalStateException(
                    "Seed login failed for role " + role + " (" + email + "): HTTP "
                            + r.statusCode() + " - " + r.asString());
        }
        String token = r.jsonPath().getString("data.token");
        if (token == null || token.isBlank()) {
            log.error("Login succeeded but token was empty for {}: {}", role, r.asString());
            throw new IllegalStateException(
                    "Login succeeded but token was empty for " + role + ": " + r.asString());
        }
        log.info("Cached JWT for role {}", role);
        return token;
    }
}
