package com.cts.mfrp.petz.utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.events.EventFiringDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

import static com.cts.mfrp.petz.constants.AppConstants.*;

public class DriverFactory {

    private static final Logger log = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void initDriver() {
        log.debug("Initializing ChromeDriver via WebDriverManager");
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--remote-allow-origins=*");

        WebDriver raw = new ChromeDriver(options);
        raw.manage().timeouts().implicitlyWait(Duration.ofSeconds(IMPLICIT_WAIT));
        raw.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(PAGE_LOAD_WAIT));

        WebDriver decorated = new EventFiringDecorator<>(new WebDriverEventLogger()).decorate(raw);
        driver.set(decorated);
        log.info("ChromeDriver started (implicitWait={}s, pageLoadWait={}s)",
                IMPLICIT_WAIT, PAGE_LOAD_WAIT);
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            log.debug("Quitting ChromeDriver");
            driver.get().quit();
            driver.remove();
        }
    }
}
