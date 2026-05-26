package com.cts.mfrp.petz.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

/**
 * Reads .xlsx test-data files from the classpath into a Map keyed by caseId.
 * Each row becomes Map&lt;columnName, cellValue&gt;.
 */
public class ExcelDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(ExcelDataProvider.class);

    /**
     * Returns all rows from the given sheet as a List of Maps (column-name → cell-string).
     */
    public static List<Map<String, String>> readSheet(String classpathPath, String sheetName) {
        List<Map<String, String>> rows = new ArrayList<>();

        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathPath)) {
            if (is == null) {
                throw new RuntimeException("Excel file not found on classpath: " + classpathPath);
            }
            try (Workbook workbook = new XSSFWorkbook(is)) {
                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new RuntimeException("Sheet '" + sheetName + "' not found in " + classpathPath);
                }

                Row header = sheet.getRow(0);
                if (header == null) {
                    throw new RuntimeException("Sheet '" + sheetName + "' has no header row");
                }

                List<String> columns = new ArrayList<>();
                for (Cell c : header) columns.add(cellAsString(c));

                int lastRow = sheet.getLastRowNum();
                for (int i = 1; i <= lastRow; i++) {
                    Row row = sheet.getRow(i);
                    if (row == null) continue;
                    Map<String, String> rowMap = new LinkedHashMap<>();
                    boolean anyValue = false;
                    for (int j = 0; j < columns.size(); j++) {
                        String key = columns.get(j);
                        String val = cellAsString(row.getCell(j));
                        rowMap.put(key, val);
                        if (val != null && !val.isEmpty()) anyValue = true;
                    }
                    if (anyValue) rows.add(rowMap);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel sheet " + sheetName + " from " + classpathPath, e);
        }

        logger.info("Read {} rows from {}::{}", rows.size(), classpathPath, sheetName);
        return rows;
    }

    /**
     * Find one row by its caseId column. Returns null if not found.
     */
    public static Map<String, String> findByCaseId(String classpathPath, String sheetName, String caseId) {
        for (Map<String, String> row : readSheet(classpathPath, sheetName)) {
            if (caseId.equals(row.get("caseId"))) return row;
        }
        return null;
    }

    private static String cellAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:  return cell.getStringCellValue();
            case NUMERIC: return DateUtil.isCellDateFormatted(cell)
                    ? cell.getDateCellValue().toString()
                    : trimTrailingZero(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA: return cell.getCellFormula();
            case BLANK:
            default:      return "";
        }
    }

    private static String trimTrailingZero(double d) {
        if (d == (long) d) return String.valueOf((long) d);
        return String.valueOf(d);
    }

    private ExcelDataProvider() {}
}
