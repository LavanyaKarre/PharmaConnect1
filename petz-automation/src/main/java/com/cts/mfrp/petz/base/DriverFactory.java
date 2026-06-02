package com.cts.mfrp.petz.base;

import com.cts.mfrp.petz.constants.AppConstants;
import com.cts.mfrp.petz.constants.BrowserType;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

/**
 * ThreadLocal-backed WebDriver factory. Sequential v1 but already safe for parallel scenarios.
 * Browser is picked via -Dbrowser=chrome|firefox|edge (default chrome).
 */
public class DriverFactory {

    private static final Logger logger = LoggerFactory.getLogger(DriverFactory.class);
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static void initDriver() {
        if (driver.get() != null) return;

        String browserProp = System.getProperty("browser", "chrome").toUpperCase();
        BrowserType browser = BrowserType.valueOf(browserProp);

        WebDriver instance;
        switch (browser) {
            case FIREFOX:
                WebDriverManager.firefoxdriver().setup();
                instance = new FirefoxDriver();
                break;
            case EDGE:
                WebDriverManager.edgedriver().setup();
                instance = new EdgeDriver();
                break;
            case CHROME:
            default:
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--disable-notifications");
                if (Boolean.parseBoolean(System.getProperty("headless", "false"))) {
                    options.addArguments("--headless=new");
                    options.addArguments("--window-size=1920,1080");
                }
                instance = new ChromeDriver(options);
                break;
        }

        instance.manage().window().maximize();
        instance.manage().timeouts().implicitlyWait(Duration.ofSeconds(AppConstants.IMPLICIT_WAIT));
        instance.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(AppConstants.PAGE_LOAD_WAIT));

        driver.set(instance);
        logger.info("Driver initialised: {}", browser);
    }

    public static WebDriver getDriver() {
        if (driver.get() == null) {
            throw new IllegalStateException("Driver not initialised. Call initDriver() first.");
        }
        return driver.get();
    }

    /** True if the current thread has an active WebDriver. Safe to call from listeners. */
    public static boolean hasDriver() {
        return driver.get() != null;
    }

    public static void quitDriver() {
        WebDriver instance = driver.get();
        if (instance != null) {
            instance.quit();
            driver.remove();
            logger.info("Driver quit");
        }
    }

    private DriverFactory() {}
}
