package com.cts.mfrp.petz.bootstrap;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * One-shot data generator. Run with:
 *   mvn -q exec:java -Dexec.classpathScope=test -Dexec.mainClass="com.cts.mfrp.petz.bootstrap.TestDataBootstrap"
 * or compile + run directly. Produces:
 *   src/test/resources/testdata/login-data.xlsx
 *   src/test/resources/testdata/register-data.xlsx
 *
 * Re-running overwrites the files. Safe to commit the output.
 */
public class TestDataBootstrap {

    private static final Path OUT_DIR = Paths.get("src/test/resources/testdata");

    public static void main(String[] args) throws Exception {
        Files.createDirectories(OUT_DIR);
        writeLoginData();
        writeRegisterData();
        System.out.println("Test-data bootstrap complete.");
    }

    // ---------------------------------------------------------------------------------
    // login-data.xlsx
    //   sheet: login_validation  → TC003 rows
    //   sheet: happy_paths       → TC004 rows
    // ---------------------------------------------------------------------------------
    private static void writeLoginData() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            // ---- login_validation ----
            Sheet val = wb.createSheet("login_validation");
            writeHeader(val, "caseId", "field", "dimension", "value", "otherFieldValid",
                    "expectedButtonState", "expectedError");

            List<String[]> rows = Arrays.asList(
                    row("EMAIL_BVA_EMPTY",    "email",    "BVA",    "",                                  "YES", "DISABLED", "Email is required"),
                    row("EMAIL_BVA_MIN",      "email",    "BVA",    "a@b.co",                             "YES", "ENABLED",  ""),
                    row("EMAIL_BVA_MAX",      "email",    "BVA",    repeat("a", 60) + "@example.com",     "YES", "ENABLED",  ""),
                    row("EMAIL_EP_INVALID",   "email",    "EP",     "abcxyz",                             "YES", "DISABLED", "Email format invalid"),
                    row("EMAIL_EP_VALID",     "email",    "EP",     "valid.user@petz.com",                "YES", "ENABLED",  ""),
                    row("EMAIL_MONKEY_SQLI",  "email",    "MONKEY", "' OR '1'='1",                        "YES", "DISABLED", "Email format invalid"),
                    row("EMAIL_MONKEY_XSS",   "email",    "MONKEY", "<script>alert('xss')</script>",      "YES", "DISABLED", "Email format invalid"),
                    row("EMAIL_MONKEY_UNICODE","email",   "MONKEY", "用户@petz.com",                       "YES", "DISABLED", "Email format invalid"),
                    row("PWD_BVA_EMPTY",      "password", "BVA",    "",                                   "YES", "DISABLED", "Password is required"),
                    row("PWD_BVA_MIN",        "password", "BVA",    "a",                                  "YES", "ENABLED",  ""),
                    row("PWD_EP_VALID",       "password", "EP",     "Admin@petz123",                      "YES", "ENABLED",  ""),
                    row("PWD_MONKEY_SQLI",    "password", "MONKEY", "' OR '1'='1",                        "YES", "ENABLED",  "")
            );
            int r = 1;
            for (String[] data : rows) writeRow(val, r++, data);
            autosize(val, 7);

            // ---- happy_paths ----
            Sheet hp = wb.createSheet("happy_paths");
            writeHeader(hp, "caseId", "role", "email", "password", "expectedUrl", "expectedGreetingContains");
            List<String[]> hpRows = Arrays.asList(
                    row("LOGIN_OK_PETOWNER", "PET_OWNER", "user@petz.com",     "admin@petz123", "/dashboard", ""),
                    row("LOGIN_OK_NGO",      "NGO",       "ngo@petz.com",      "admin@petz123", "/ngo",       ""),
                    row("LOGIN_OK_HOSPITAL", "HOSPITAL",  "hospital@petz.com", "admin@petz123", "/hospital",  "")
            );
            int hr = 1;
            for (String[] data : hpRows) writeRow(hp, hr++, data);
            autosize(hp, 6);

            // ---- login_errors (TC005) ----
            Sheet le = wb.createSheet("login_errors");
            writeHeader(le, "caseId", "email", "password", "expectedUrlContains");
            List<String[]> leRows = Arrays.asList(
                    row("WRONG_PWD",    "user@petz.com",    "wrong-pass",     "/auth/login"),
                    row("UNKNOWN_USER", "unknown@petz.com", "admin@petz123",  "/auth/login"),
                    row("EMPTY_BOTH",   "",                 "",               "/auth/login")
            );
            int ler = 1;
            for (String[] data : leRows) writeRow(le, ler++, data);
            autosize(le, 4);

            try (FileOutputStream out = new FileOutputStream(OUT_DIR.resolve("login-data.xlsx").toFile())) {
                wb.write(out);
            }
            System.out.println("Wrote login-data.xlsx ("
                    + rows.size() + " validation + "
                    + hpRows.size() + " happy + "
                    + leRows.size() + " error rows)");
        }
    }

    // ---------------------------------------------------------------------------------
    // register-data.xlsx
    //   sheet: register_validation  → TC006 rows
    // ---------------------------------------------------------------------------------
    private static void writeRegisterData() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("register_validation");
            writeHeader(sheet, "caseId", "targetField", "dimension", "value",
                    "expectedButtonState", "expectedError");

            List<String[]> rows = Arrays.asList(
                    row("NAME_BVA_EMPTY",       "fullName",        "BVA",    "",                              "DISABLED", "Name is required"),
                    row("NAME_BVA_MIN",         "fullName",        "BVA",    "Al",                             "ENABLED",  ""),
                    row("NAME_BVA_MAX",         "fullName",        "BVA",    repeat("A", 100),                 "ENABLED",  ""),
                    row("PHONE_EP_VALID",       "phone",           "EP",     "9876543210",                     "ENABLED",  ""),
                    row("PHONE_EP_INVALID",     "phone",           "EP",     "abcdef",                         "DISABLED", "Invalid phone"),
                    row("PHONE_BVA_SHORT",      "phone",           "BVA",    "98765",                          "DISABLED", "Phone too short"),
                    row("EMAIL_EP_INVALID",     "email",           "EP",     "bad-email",                      "DISABLED", "Email format invalid"),
                    row("EMAIL_EP_VALID",       "email",           "EP",     "new.user@petz.com",              "ENABLED",  ""),
                    row("PWD_BVA_WEAK",         "password",        "BVA",    "12",                             "DISABLED", "Password too short"),
                    row("PWD_BVA_STRONG",       "password",        "BVA",    "Admin@123",                      "ENABLED",  ""),
                    row("CONFIRM_MISMATCH",     "confirmPassword", "BVA",    "Mismatch@123",                   "DISABLED", "Passwords do not match"),
                    row("ROLE_MISSING",         "accountType",     "BVA",    "",                               "DISABLED", "Role is required"),
                    row("NAME_MONKEY_XSS",      "fullName",        "MONKEY", "<script>alert('xss')</script>",  "DISABLED", "Invalid characters"),
                    row("EMAIL_MONKEY_SQLI",    "email",           "MONKEY", "' OR '1'='1",                    "DISABLED", "Email format invalid"),
                    row("DESC_MONKEY_UNICODE",  "fullName",        "MONKEY", "用户 名 🐶",                       "ENABLED",  "")
            );
            int r = 1;
            for (String[] data : rows) writeRow(sheet, r++, data);
            autosize(sheet, 6);

            try (FileOutputStream out = new FileOutputStream(OUT_DIR.resolve("register-data.xlsx").toFile())) {
                wb.write(out);
            }
            System.out.println("Wrote register-data.xlsx (" + rows.size() + " validation rows)");
        }
    }

    // ---------- helpers ----------

    private static void writeHeader(Sheet sheet, String... cols) {
        Row r = sheet.createRow(0);
        CellStyle bold = sheet.getWorkbook().createCellStyle();
        Font f = sheet.getWorkbook().createFont(); f.setBold(true); bold.setFont(f);
        for (int i = 0; i < cols.length; i++) {
            Cell c = r.createCell(i);
            c.setCellValue(cols[i]);
            c.setCellStyle(bold);
        }
    }

    private static void writeRow(Sheet sheet, int rowIndex, String[] values) {
        Row r = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            r.createCell(i).setCellValue(values[i]);
        }
    }

    private static void autosize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) sheet.autoSizeColumn(i);
    }

    private static String[] row(String... vals) { return vals; }

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder(s.length() * n);
        for (int i = 0; i < n; i++) sb.append(s);
        return sb.toString();
    }
}
