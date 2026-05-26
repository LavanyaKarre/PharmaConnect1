package com.cts.mfrp.petz.constants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Holds environment-level config (URLs, timeouts, seed credentials).
 *
 * Loader precedence per key:
 *   1. -D system property (e.g., -DpetOwnerEmail=alice@petz.com)
 *   2. src/main/resources/application.properties
 *   3. hardcoded fallback in this file (last-resort safety net so the test
 *      framework never fails to bootstrap because a property is missing)
 *
 * Per-case test data lives in Excel/XML fixtures under src/test/resources/testdata/.
 */
public class AppConstants {

    private static final Logger logger = LoggerFactory.getLogger(AppConstants.class);
    private static final Properties PROPS = loadProperties();

    // ---- Application under test ----
    public static final String BASE_URL     = get("baseUrl", "https://stellular-taffy-e3ee7a.netlify.app");
    public static final String HOME_URL     = BASE_URL + "/";
    public static final String LOGIN_URL    = BASE_URL + "/auth/login";
    public static final String REGISTER_URL = BASE_URL + "/auth/register";

    // Pet Owner routes
    public static final String DASHBOARD_URL         = BASE_URL + "/dashboard";
    public static final String ADOPTION_ANIMALS_URL  = BASE_URL + "/adoption/animals";
    public static final String ADOPTION_MY_URL       = BASE_URL + "/adoption/my";
    public static final String APPOINTMENTS_URL      = BASE_URL + "/appointments";
    public static final String APPOINTMENTS_BOOK_URL = BASE_URL + "/appointments/book";
    public static final String RESCUE_URL            = BASE_URL + "/rescue";
    public static final String RESCUE_REPORT_URL     = BASE_URL + "/rescue/report";
    public static final String PETS_URL              = BASE_URL + "/pets";

    // NGO routes
    public static final String NGO_URL              = BASE_URL + "/ngo";
    public static final String NGO_ANIMALS_URL      = BASE_URL + "/ngo/animals";
    public static final String NGO_APPLICATIONS_URL = BASE_URL + "/ngo/applications";
    public static final String NGO_RESCUES_URL      = BASE_URL + "/ngo/rescues";

    // Hospital routes
    public static final String HOSPITAL_URL              = BASE_URL + "/hospital";
    public static final String HOSPITAL_APPOINTMENTS_URL = BASE_URL + "/hospital/appointments";
    public static final String HOSPITAL_DOCTORS_URL      = BASE_URL + "/hospital/doctors";

    // ---- Timeouts (seconds) ----
    public static final int IMPLICIT_WAIT  = getInt("implicitWait", 10);
    public static final int EXPLICIT_WAIT  = getInt("explicitWait", 15);
    public static final int LOGIN_WAIT     = getInt("loginWait", 30);
    public static final int PAGE_LOAD_WAIT = getInt("pageLoadWait", 30);

    // ---- Seed credentials (NEVER hardcoded; sourced from properties / -D) ----
    public static final String PET_OWNER_EMAIL    = get("petOwnerEmail",    "user@petz.com");
    public static final String PET_OWNER_PASSWORD = get("petOwnerPassword", "admin@petz123");
    public static final String PET_OWNER_NAME     = get("petOwnerName",     "Test");

    public static final String NGO_EMAIL          = get("ngoEmail",         "ngo@petz.com");
    public static final String NGO_PASSWORD       = get("ngoPassword",      "admin@petz123");

    public static final String HOSPITAL_EMAIL     = get("hospitalEmail",    "hospital@petz.com");
    public static final String HOSPITAL_PASSWORD  = get("hospitalPassword", "admin@petz123");

    // ---- Output paths ----
    public static final String SCREENSHOT_DIR = get("screenshotDir", "test-output/screenshots/");

    // ---- Test-data file locations (resource paths, intentionally fixed) ----
    public static final String LOGIN_DATA_XLSX    = "testdata/login-data.xlsx";
    public static final String REGISTER_DATA_XLSX = "testdata/register-data.xlsx";
    public static final String LANDING_DATA_XML   = "testdata/landing-data.xml";
    public static final String USER_DATA_XML      = "testdata/user-data.xml";
    public static final String NGO_DATA_XML       = "testdata/ngo-data.xml";
    public static final String HOSPITAL_DATA_XML  = "testdata/hospital-data.xml";

    // ---- helpers ----

    private static String get(String key, String fallback) {
        String fromSys = System.getProperty(key);
        if (fromSys != null && !fromSys.isEmpty()) return fromSys;
        return PROPS.getProperty(key, fallback);
    }

    private static int getInt(String key, int fallback) {
        try { return Integer.parseInt(get(key, String.valueOf(fallback))); }
        catch (NumberFormatException e) { return fallback; }
    }

    private static Properties loadProperties() {
        Properties p = new Properties();
        try (InputStream is = AppConstants.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                p.load(is);
                logger.info("Loaded {} keys from application.properties", p.size());
            } else {
                logger.warn("application.properties not found on classpath — using hardcoded fallbacks");
            }
        } catch (Exception e) {
            logger.warn("Failed to read application.properties: {}", e.getMessage());
        }
        return p;
    }

    private AppConstants() {}
}
