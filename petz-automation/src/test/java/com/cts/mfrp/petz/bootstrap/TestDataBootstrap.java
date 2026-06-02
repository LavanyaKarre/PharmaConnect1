package com.cts.mfrp.petz.bootstrap;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * One-shot generator for the only .xlsx fixture this suite still uses:
 *   src/test/resources/testdata/register-data.xlsx (sheet: happy_path)
 *
 * Run with:
 *   mvn -q exec:java -Dexec.mainClass="com.cts.mfrp.petz.bootstrap.TestDataBootstrap"
 *
 * Re-running overwrites the file. Safe to commit the output.
 *
 * petz.basedir is set by the exec-maven-plugin (see pom.xml) so the output path
 * resolves correctly even when mvn is invoked from a parent directory.
 */
public class TestDataBootstrap {

    private static final Path OUT_DIR =
            Paths.get(System.getProperty("petz.basedir", "."), "src/test/resources/testdata");

    public static void main(String[] args) throws Exception {
        Files.createDirectories(OUT_DIR);
        writeRegisterData();
        System.out.println("Test-data bootstrap complete.");
    }

    // ---------------------------------------------------------------------------------
    // register-data.xlsx
    //   sheet: happy_path  → TC02 row (one happy-path registration; email generated inline by the test)
    // ---------------------------------------------------------------------------------
    private static void writeRegisterData() throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("happy_path");
            writeHeader(sheet, "caseId", "fullName", "phone", "password", "accountType");

            writeRow(sheet, 1, "REGISTER_HAPPY", "Test User", "9876543210", "Admin@123", "Pet Owner");
            autosize(sheet, 5);

            try (FileOutputStream out = new FileOutputStream(OUT_DIR.resolve("register-data.xlsx").toFile())) {
                wb.write(out);
            }
            System.out.println("Wrote register-data.xlsx (1 happy-path row)");
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

    private static void writeRow(Sheet sheet, int rowIndex, String... values) {
        Row r = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            r.createCell(i).setCellValue(values[i]);
        }
    }

    private static void autosize(Sheet sheet, int columns) {
        for (int i = 0; i < columns; i++) sheet.autoSizeColumn(i);
    }
}
