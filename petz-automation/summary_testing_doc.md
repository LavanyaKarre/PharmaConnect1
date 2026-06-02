# PETZ Automation — System Summary (the whole ins & outs)

A single reference that explains **every component** of the `petz-automation` project, **what each does**, **how they link together**, **how the whole thing runs**, the **commands**, and the **things to keep in mind**. If you read only one doc to understand this project, read this one. (`commands_for_testing.md` is the quick command cheat-sheet; this is the full picture.)

---

## 1. What this project is

A UI test-automation suite for the **PETZ** web app (an Angular animal-welfare platform: pet owners, NGOs, hospitals). It drives a real browser against the live deployment and verifies the key end-to-end journeys.

- **Application under test (AUT):** `https://stellular-taffy-e3ee7a.netlify.app` (configurable).
- **What it tests:** 15 test cases (TC01–TC15) told as an **end-to-end narrative** across four roles.
- **Two ways to run the same 15 tests:**
  1. **TestNG** suite — 4 Java "journey" classes (the default).
  2. **Cucumber BDD** suite — the same 15 as Gherkin `.feature` scenarios (profile-activated).
- Both suites **share one framework core** (Page Objects, driver, config, data readers). Only the *test layer* differs.

---

## 2. Tech stack

| Concern | Choice |
|---|---|
| Language / build | Java 17, Maven (`com.cts.mfrp:petz-automation:3.0.0`, packaging `jar`) |
| Browser automation | Selenium 4.18.1 + WebDriverManager 5.7.0 |
| Test runner | TestNG 7.9.0 |
| BDD layer | Cucumber 7.18.0 (`cucumber-java` + `cucumber-testng`) |
| Test data | Apache POI 5.2.5 (`.xlsx`) + DOM-based XML reader (`.xml`) |
| Reporting (TestNG) | ExtentReports 5.1.1 (Spark, dark theme, base64 screenshots) |
| Reporting (Cucumber) | Built-in HTML/JSON + `maven-cucumber-reporting` 5.8.0 dashboard |
| Logging | SLF4J 2.0.13 + Logback 1.5.6 |

---

## 3. The big picture — one core, two test layers

```
                        ┌─────────────────────────────────────────────┐
                        │              SHARED CORE  (src/main)          │
                        │                                               │
                        │  base/      DriverFactory · BasePage · WaitUtil
                        │  pages/     ~18 Page Objects (one per screen)  │
                        │  utils/     Excel/XmlDataProvider · ExtentMgr  │
                        │  constants/ AppConstants · UserRole · enums    │
                        │  resources/ application.properties             │
                        └───────────────▲───────────────────▲───────────┘
                                        │ reuses            │ reuses
                  ┌─────────────────────┴──────┐   ┌────────┴───────────────────┐
                  │   TestNG suite (src/test)   │   │  Cucumber suite (src/test) │
                  │                             │   │                            │
                  │  tests/      4 journeys     │   │  features/   4 .feature    │
                  │  base/       BaseTest       │   │  cucumber/   runner        │
                  │  listeners/  TestListener   │   │              hooks         │
                  │  bootstrap/  TestDataBootstrap   │              steps        │
                  │                             │   │                            │
                  │  testng.xml  →  mvn test    │   │  testng-cucumber.xml       │
                  │                             │   │       →  mvn verify -Pcucumber
                  └─────────────────────────────┘   └────────────────────────────┘
```

**Key idea:** a Page Object (e.g. `LoginPage`, `PetManagementPage`) knows *how to operate one screen*. Both the TestNG tests and the Cucumber step definitions call those same Page Objects. So a selector change is fixed **once** and both suites benefit.

---

## 4. Directory layout (annotated)

```
petz-automation/
├── pom.xml                     ← deps, surefire (runs ${suiteXmlFile}), exec plugin, "cucumber" profile
├── testng.xml                  ← TestNG suite: 4 journey classes, preserve-order=true, TestListener
├── testng-cucumber.xml         ← Cucumber suite: the single RunCucumberTest class
├── commands_for_testing.md     ← quick command reference
├── summary_testing_doc.md      ← THIS FILE
│
├── src/main/java/com/cts/mfrp/petz/      ← SHARED CORE (framework, no tests)
│   ├── base/
│   │   ├── DriverFactory.java            ← creates/holds the WebDriver (ThreadLocal)
│   │   ├── BasePage.java                 ← find/click/type/wait helpers + auto step-logging
│   │   └── WaitUtil.java                 ← static WebDriverWait helpers
│   ├── pages/                            ← one Page Object per screen (see §5.3)
│   ├── utils/
│   │   ├── ExcelDataProvider.java        ← reads .xlsx → List<Map<col,val>>
│   │   ├── XmlDataProvider.java          ← reads .xml  → List<Map<attr,val>>
│   │   └── ExtentReportManager.java      ← ExtentReports singleton + logStep()
│   └── constants/
│       ├── AppConstants.java             ← URLs, timeouts, seed creds, file paths
│       ├── UserRole.java                 ← PET_OWNER / NGO / HOSPITAL / ANONYMOUS
│       ├── BrowserType.java              ← CHROME / FIREFOX / EDGE
│       └── Environment.java              ← DEV / STAGING / PROD (enum, available)
│
├── src/main/resources/
│   └── application.properties            ← all runtime config (baseUrl, creds, timeouts)
│
├── src/test/java/com/cts/mfrp/petz/
│   ├── base/BaseTest.java                ← TestNG lifecycle: @BeforeClass driver+login, @AfterClass quit
│   ├── listeners/TestListener.java       ← TestNG→Extent bridge + per-test screenshot
│   ├── bootstrap/TestDataBootstrap.java  ← regenerates register-data.xlsx (run once)
│   ├── tests/                            ← TestNG suite (4 classes, 15 TCs)
│   │   ├── PublicJourneyTest.java        (TC01–TC02)
│   │   ├── PetOwnerJourneyTest.java      (TC03–TC09)
│   │   ├── NGOJourneyTest.java           (TC10–TC12)
│   │   └── HospitalJourneyTest.java      (TC13–TC15)
│   └── cucumber/                         ← Cucumber suite (same 15 TCs)
│       ├── runner/RunCucumberTest.java   ← @CucumberOptions, extends AbstractTestNGCucumberTests
│       ├── hooks/Hooks.java              ← @BeforeAll/@AfterAll driver, @After screenshot
│       └── steps/                        ← step definitions grouped by domain
│           ├── CommonSteps.java  AuthSteps.java
│           ├── LandingSteps.java PetOwnerSteps.java
│           └── NgoSteps.java     HospitalSteps.java
│
└── src/test/resources/
    ├── features/                         ← Gherkin (numbered to preserve order)
    │   ├── 01_public.feature  02_pet_owner.feature
    │   └── 03_ngo.feature     04_hospital.feature
    ├── testdata/
    │   ├── register-data.xlsx            ← sheet: happy_path (TC02)  [generated]
    │   ├── login-data.xlsx               ← present; journeys use seed creds instead
    │   ├── user-data.xml                 ← pets / appointmentBooking / rescues / adoptionApplications
    │   ├── ngo-data.xml                  ← animals (TC11)
    │   └── hospital-data.xml             ← doctors (TC14)
    └── logback.xml                       ← logging config (console + automation.log)
```

---

## 5. Component reference — what each piece does

### 5.1 Shared core › `base/`

- **`DriverFactory`** — Creates the WebDriver based on `-Dbrowser` (chrome/firefox/edge, default chrome) and `-Dheadless`. Stores it in a **`ThreadLocal`** so each thread has its own browser (parallel-safe). Applies window maximize, implicit wait, page-load timeout. Exposes `initDriver()`, `getDriver()`, `hasDriver()`, `quitDriver()`. **Single source of the browser instance** for the whole framework — both suites and the listener pull the driver from here.
- **`BasePage`** — Abstract superclass every Page Object extends. Hides the Selenium plumbing: `find/findVisible/findClickable/findAll`, `click/type/select*`, `isVisible/isPresent/isEnabled`, `goTo`, `scrollIntoView`, etc. Builds a `WebDriverWait` from `AppConstants.EXPLICIT_WAIT`. **Also auto-logs each `goTo/click/type/select` as a report step** via `ExtentReportManager.logStep(...)` (passwords masked) — this is why reports show step-by-step detail with no per-test code.
- **`WaitUtil`** — Static convenience `WebDriverWait` helpers (`waitVisible`, `waitClickable`, `waitUrlContains`, custom seconds). Used where an explicit, ad-hoc wait is clearer than the BasePage one (e.g. `LoginPage.submitAndWait` waits 30 s for the URL to leave `/auth/login`).

### 5.2 Shared core › `constants/`

- **`AppConstants`** — The config hub. Exposes all URLs (`LOGIN_URL`, `DASHBOARD_URL`, `NGO_URL`, …), timeouts, **seed credentials**, screenshot dir, and test-data resource paths. Loads values with **precedence: `-D` system property → `application.properties` → hardcoded fallback**. Everything reads config through here.
- **`UserRole`** — `PET_OWNER`, `NGO`, `HOSPITAL`, `ANONYMOUS`. Drives which seed login `BaseTest`/`AuthSteps` perform.
- **`BrowserType`** — `CHROME`, `FIREFOX`, `EDGE`. Parsed by `DriverFactory`.
- **`Environment`** — `DEV`, `STAGING`, `PROD` enum (available for environment switching).

### 5.3 Shared core › `pages/` (Page Object Model)

One class per screen; each exposes intention-revealing methods (`fillName`, `clickSave`, `getCardCount`) and hides selectors. The 15 TCs touch these:

| Page Object | Screen / role | Used by |
|---|---|---|
| `LandingPage` | public `/` | TC01 |
| `RegisterPage` | `/auth/register` | TC02 |
| `LoginPage` | `/auth/login` (+ `loginAs(role)`) | all logins |
| `UserDashboardPage` | pet-owner `/dashboard` | TC03 |
| `PetManagementPage` | `/pets` | TC04 |
| `BookAppointmentPage` | `/appointments/book` | TC05 |
| `ReportRescuePage` | `/rescue/report` | TC06 |
| `BrowseAdoptionPage` | `/adoption/animals` | TC07, TC08 |
| `MyAppointmentsPage` | `/appointments` | TC09 |
| `MyApplicationsPage` | `/adoption/my` | TC09 |
| `NGODashboardPage` | `/ngo` | TC10 |
| `NGOAnimalsPage` | `/ngo/animals` | TC11 |
| `AdoptionApplicationsPage` | `/ngo/applications` | TC12 |
| `HospitalDashboardPage` | `/hospital` | TC13 |
| `ManageDoctorsPage` | `/hospital/doctors` | TC14 |
| `HospitalAppointmentsPage` | `/hospital/appointments` | TC15 |

(`RescueQueuePage`, `RescueReportsListPage` also exist as auxiliary objects.)

### 5.4 Shared core › `utils/`

- **`ExcelDataProvider`** — `readSheet(path, sheetName)` → `List<Map<column,value>>`; `findByCaseId(...)`. Reads `.xlsx` off the classpath via POI. Numbers/booleans/formulas coerced to strings; blank rows skipped.
- **`XmlDataProvider`** — `readSection(path, sectionName)` → `List<Map<attribute,value>>`; `findByCaseId(...)`. DOM parser, **DOCTYPE disabled** (XXE-safe). Same `List<Map>` shape as Excel so callers use one mental model.
- **`ExtentReportManager`** — Singleton holder for the run-scoped `ExtentReports` + a `ThreadLocal<ExtentTest>` for the in-flight test. Configures the Spark report (dark theme, **`setOfflineMode(true)`**, system-info rows). `logStep(msg)` appends an info step to the current test **and no-ops when none is active** (so `BasePage` logging is safe under Cucumber).

### 5.5 Shared core › `resources/application.properties`

The default config file (baseUrl, browser, headless, timeouts, seed creds). Overridden per-key by `-D` flags. See §8.

### 5.6 TestNG suite › `src/test`

- **`BaseTest`** — Superclass of the 4 journey classes. `@BeforeClass`: `DriverFactory.initDriver()` then, if `role() != null`, auto-login via `LoginPage.loginAs(role)`. `@AfterClass`: quit. **One browser + one login shared across all TCs in a class** — that's how each role logs in exactly once.
- **`TestListener`** (`ITestListener`, registered in `testng.xml`) — Bridges TestNG → ExtentReports. On suite start inits the report; per test creates an `ExtentTest`, tags it with the **journey class as a category**, logs the data row; on pass/fail captures a screenshot, **embeds it as base64**, and writes a standalone `.png`; on finish flushes the report.
- **`TestDataBootstrap`** — `main()` that regenerates `register-data.xlsx`. Run once (or after editing). Anchored via the `petz.basedir` system property set by the exec plugin.
- **The 4 journey classes** — `PublicJourneyTest` (anonymous), `PetOwnerJourneyTest`, `NGOJourneyTest`, `HospitalJourneyTest`. Each `@Test` is `TCxx_…` ordered by `priority`; data-driven ones use a `@DataProvider` that reads the matching xml/xlsx fixture.

### 5.7 Cucumber suite › `src/test`

- **`RunCucumberTest`** — `@CucumberOptions(features="classpath:features", glue={hooks, steps}, plugin={pretty, html, json})`, extends `AbstractTestNGCucumberTests` so TestNG/surefire executes each scenario.
- **`Hooks`** — `@BeforeAll` starts the browser **once for the whole run**, `@AfterAll` quits it. `@After` attaches a **base64 screenshot** of the final page to each scenario (`scenario.attach`).
- **`AuthSteps`** — Implements the `Given I am logged in as a <role>` Background. Tracks the logged-in role in a **`static` field** and only re-logs-in when the role changes → **one login per journey** (3 logins total), matching the TestNG behaviour even though Cucumber scenarios are independent.
- **`CommonSteps`** — Reusable `Then I should be on the "<path>" page` / login-or-dashboard assertions (reads the driver directly).
- **`LandingSteps` / `PetOwnerSteps` / `NgoSteps` / `HospitalSteps`** — Domain steps that call the **same Page Objects** as the TestNG tests; form data arrives from Gherkin **data tables**.
- **`features/0X_*.feature`** — The 15 scenarios in Gherkin, numbered `01`–`04` so lexical order preserves the journey narrative.

### 5.8 Root config files

- **`pom.xml`** — Dependencies; surefire runs `${suiteXmlFile}` (default `testng.xml`); exec plugin for the bootstrap; **`cucumber` profile** that flips `${suiteXmlFile}` → `testng-cucumber.xml` and adds the dashboard plugin (bound to `verify`).
- **`testng.xml`** — The TestNG suite: 4 classes in fixed order, `preserve-order="true"`, `TestListener` registered.
- **`testng-cucumber.xml`** — The Cucumber suite: just `RunCucumberTest` (no listener — Cucumber plugins produce the report).

---

## 6. How it all links — execution flow

### 6.1 TestNG run (`mvn test`)
1. Surefire reads `testng.xml` → registers `TestListener`, runs the 4 classes in order.
2. For each class: `BaseTest.@BeforeClass` → `DriverFactory.initDriver()` → (if role) `LoginPage.loginAs(role)`.
3. Each `@Test` opens Page Objects, which call `BasePage` helpers; every action auto-logs a step to Extent and resolves the live page.
4. `TestListener` fires per test: creates the Extent entry, captures + embeds a base64 screenshot, records pass/fail.
5. `@AfterClass` quits the browser; next class repeats with its own login.
6. On suite finish, `ExtentReportManager.flush()` writes `test-output/reports/ExtentReport.html`.

### 6.2 Cucumber run (`mvn verify -Pcucumber`)
1. The `cucumber` profile sets `${suiteXmlFile}=testng-cucumber.xml`; surefire runs `RunCucumberTest`.
2. `Hooks.@BeforeAll` starts **one** browser for the whole run.
3. Cucumber executes features `01→04`. Each role feature's `Background` calls an `AuthSteps` login step, which **logs in only when the role changes** (3 logins total).
4. Step definitions drive the **same Page Objects**; `BasePage.logStep` no-ops (no Extent active).
5. `Hooks.@After` attaches a base64 screenshot to each scenario; Cucumber writes `target/cucumber-report.html` + `target/cucumber.json`.
6. At the `verify` phase, `maven-cucumber-reporting` reads the JSON → builds `target/cucumber-html-reports/overview-features.html`.
7. `Hooks.@AfterAll` quits the browser.

---

## 7. The 15 test cases

| TC | Journey | What it verifies | Data source |
|----|---------|------------------|-------------|
| TC01 | Public | Landing renders: logo, hero, stats, features, footer | — |
| TC02 | Public | New user registers (fresh inline email) | `register-data.xlsx::happy_path` |
| TC03 | Pet Owner | `/dashboard`: greeting, emergency banner, KPI tile | — |
| TC04 | Pet Owner | Add a pet | `user-data.xml::pets` |
| TC05 | Pet Owner | Book a vet appointment (hospital→doctor→date→time→reason) | `user-data.xml::appointmentBooking` |
| TC06 | Pet Owner | Report a rescue | `user-data.xml::rescues` |
| TC07 | Pet Owner | Browse adoption listings | — |
| TC08 | Pet Owner | Apply to adopt the first animal | `user-data.xml::adoptionApplications` |
| TC09 | Pet Owner | My Appointments + My Applications render | — |
| TC10 | NGO | `/ngo`: stat tiles + quick actions | — |
| TC11 | NGO | Add an adoptable animal | `ngo-data.xml::animals` |
| TC12 | NGO | Adoption applications screen renders | — |
| TC13 | Hospital | `/hospital` dashboard renders | — |
| TC14 | Hospital | Add a doctor | `hospital-data.xml::doctors` |
| TC15 | Hospital | Appointments queue renders; confirm first if present | — |

**Non-gating note:** TC05/TC08/TC12/TC15 are intentionally lenient — bookings/applications may route to a different hospital/NGO than the seed account, so they log counts and pass if the page rendered. Don't "tighten" them to hard-assert counts against the live site.

---

## 8. Configuration & precedence

All runtime config lives in `src/main/resources/application.properties`, read through `AppConstants`:

```
1. -D system property      (CLI override — highest)
2. application.properties
3. hardcoded fallback in AppConstants
```

Common keys: `baseUrl`, `browser`, `headless`, `implicitWait`, `explicitWait`, `loginWait` (30), `pageLoadWait`, and seed creds (`petOwnerEmail`/`Password`, `ngoEmail`/`Password`, `hospitalEmail`/`Password` — all default to `*@petz.com` / `admin@petz123`).

Override examples:
```powershell
mvn test '"-DbaseUrl=https://staging.petz.example.com"'
mvn test '"-Dbrowser=firefox"'
mvn test '"-Dheadless=true"'
```
> **PowerShell quoting:** wrap each `-D` arg as `'"-Dkey=value"'`. On bash use plain `-Dkey=value`.

---

## 9. Commands

### First-time setup
```powershell
mvn clean test-compile                                                            # 1. compile
mvn exec:java '"-Dexec.mainClass=com.cts.mfrp.petz.bootstrap.TestDataBootstrap"'  # 2. generate register-data.xlsx (one-shot)
```

### TestNG suite (default)
```powershell
mvn test                                  # all 15 TCs, visible browser
mvn test '"-Dheadless=true"'              # headless (CI)
mvn test '"-Dtest=PetOwnerJourneyTest"'   # one journey class
mvn test '"-Dbrowser=firefox"'            # different browser
```
Report → `test-output/reports/ExtentReport.html`

### Cucumber suite (profile)
```powershell
mvn verify -Pcucumber                       # all 15 scenarios + dashboard
mvn verify -Pcucumber '"-Dheadless=true"'   # headless
mvn test -Pcucumber                          # scenarios only (NO dashboard — use verify for it)
```
Reports → `target/cucumber-report.html` and `target/cucumber-html-reports/overview-features.html`

> Use **`verify`** (not `test`) for Cucumber so `maven-cucumber-reporting` runs at the verify phase.

---

## 10. Reports & artifacts — where things land

| Suite | Artifact | Path |
|---|---|---|
| TestNG | ExtentReport (self-contained, base64 screenshots, offline) | `test-output/reports/ExtentReport.html` |
| TestNG | Standalone screenshots | `test-output/screenshots/<TestName>_<PASS\|FAIL>_<stamp>.png` |
| TestNG | Run log | `test-output/automation.log` |
| Both | TestNG raw XML (CI) | `target/surefire-reports/` |
| Cucumber | Built-in HTML (steps + screenshots) | `target/cucumber-report.html` |
| Cucumber | Dashboard (pie charts, overview) | `target/cucumber-html-reports/overview-features.html` |
| Cucumber | Raw JSON (feeds dashboard) | `target/cucumber.json` |

`test-output/` survives `mvn clean`; everything under `target/` does not.

---

## 11. Things to keep in mind (load-bearing — don't regress)

**Reporting**
- **Screenshots are base64-embedded, never file paths.** Absolute Windows paths (`src="C:\...png"`) don't render as `<img>` in a browser. `TestListener` uses `createScreenCaptureFromBase64String`; Cucumber uses `scenario.attach(bytes, "image/png", …)`. Do **not** switch back to path-based embedding.
- **Extent offline mode is on** (`setOfflineMode(true)`) so the report is styled without internet/CDN.
- **Auto step-logging** lives in `BasePage`; `ExtentReportManager.logStep` no-ops when no Extent test is active, so it's safe under Cucumber. `type()` masks password fields.
- For Cucumber, **do not** reintroduce the grasshopper Extent-cucumber adapter (it was dropped earlier as ugly/step-less); the built-in HTML + dashboard is the chosen path.

**Browser / timing**
- **Login uses a 30 s wait** (`AppConstants.LOGIN_WAIT`) — cold first login on the live Netlify site is slow. Don't shorten it.
- **NGO stat tiles**: `NGODashboardPage.getStatTileCount()` does a **page-source string search** (CSS uppercases lowercase DOM) — don't replace it with a CSS/XPath selector.
- **Material icon ligatures** contaminate button text ("search Search"), so selectors use `contains(., '...')`, not `=`.
- **CTA selectors** match both `<a>` and `<button>`: `//*[self::a or self::button][contains(.,'...')]`.

**Suite behaviour**
- **TestNG order** is fixed by `testng.xml` (`preserve-order=true`) + `priority`.
- **Cucumber order** relies on lexical feature order — hence the `01_`–`04_` filename prefixes. Correctness of "one login per journey" does **not** depend on order (the static role check handles it).
- **One login per role:** TestNG via `@BeforeClass`; Cucumber via one shared browser (`@BeforeAll`/`@AfterAll`) + role-aware `AuthSteps`.
- **No inline test data in Java** — values live in `.xlsx`/`.xml` (TestNG) or Gherkin data tables (Cucumber).

---

## 12. How to extend (read first)

> **Do not add new test cases without explicit approval — the 15-TC budget is curated.**

If approved:
1. **TestNG:** add a `@Test(priority=N, description="…")` to the right journey class; if data-driven, add a `@DataProvider` + a new section/sheet in the matching fixture. Verify: `mvn test -Dtest=<JourneyClass>`.
2. **Cucumber:** add a `Scenario` to the matching `.feature` and a step method in the right `*Steps` class; reuse existing Page Objects. Verify: `mvn verify -Pcucumber`.
3. **New screen?** add a Page Object under `pages/` (extends `BasePage`) — both suites can then use it.

---

## 13. Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `Excel file not found: testdata/register-data.xlsx` | xlsx never generated | Run the `TestDataBootstrap` command (§9) |
| Screenshots blank/broken in a report | path-based embed regression | Ensure base64 embedding (§11) |
| Cucumber report has no dashboard | ran `mvn test -Pcucumber` | Use `mvn verify -Pcucumber` |
| Login times out at 15 s | someone shortened the login wait | Keep `loginWait=30` / `submitAndWait`'s 30 s |
| `Tests run: 0` for Cucumber | profile not active / wrong suite file | Add `-Pcucumber` (sets `testng-cucumber.xml`) |
| Report unstyled offline | offline mode off | `spark.config().setOfflineMode(true)` in `ExtentReportManager` |
| NGO dashboard "0 tiles" but page rendered | switched to a CSS selector | Restore page-source search in `NGODashboardPage` |

---

*Two suites, one shared core, fifteen journeys. Change a selector once in a Page Object and both the TestNG report and the Cucumber dashboard stay green.*
