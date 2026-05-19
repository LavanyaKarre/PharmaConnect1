package com.cts.mfrp.petz.utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.cts.mfrp.petz.constants.AppConstants.SCREENSHOT_PATH;

public class ScreenshotUtil {

    private static final Logger log = LoggerFactory.getLogger(ScreenshotUtil.class);

    public static String takeScreenshot(WebDriver driver, String testName) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName  = SCREENSHOT_PATH + testName + "_" + timestamp + ".png";

        File src  = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        File dest = new File(fileName);

        try {
            FileUtils.copyFile(src, dest);
            log.debug("Screenshot saved: {}", dest.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save screenshot for {}", testName, e);
        }
        return dest.getAbsolutePath();
    }
}