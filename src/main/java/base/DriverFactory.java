package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chromium.ChromiumDriver;
import utils.PropertyReader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class DriverFactory {

    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    private DriverFactory() {
        // Prevent instantiation
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

    public static void initializeDriver() {

        String browser = PropertyReader.get("browser").toLowerCase().trim();
        boolean headless = Boolean.parseBoolean(PropertyReader.get("headless"));

        // --- Use a FIXED folder outside OneDrive to avoid Chrome overrides ---
        String downloadPath = "C:\\SeleniumDownloads";
        File downloadsDir = new File(downloadPath);
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs();
        }

        switch (browser) {

            case "chrome":
                WebDriverManager.chromedriver().setup();

                // --- Chrome Preferences ---
                Map<String, Object> prefs = new HashMap<>();
                prefs.put("download.default_directory", downloadPath);
                prefs.put("download.prompt_for_download", false);
                prefs.put("download.directory_upgrade", true);

                // Disable safe browsing so Chrome does NOT block insecure downloads
                prefs.put("safebrowsing.enabled", false);
                prefs.put("safebrowsing.disable_download_protection", true);

                prefs.put("download.extensions_to_open", "xlsx");
                prefs.put("profile.default_content_setting_values.automatic_downloads", 1);

                ChromeOptions options = new ChromeOptions();
                options.setExperimentalOption("prefs", prefs);

                // Disable Chrome warnings & force insecure site to be treated as secure
                options.addArguments("--disable-features=InsecureDownloadWarnings");
                options.addArguments("--safebrowsing-disable-download-protection");
                options.addArguments("--allow-running-insecure-content");
                options.addArguments("--unsafely-treat-insecure-origin-as-secure=http://192.168.1.30");
                options.addArguments("--allow-insecure-localhost");

                // --- Stability options ---
                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--disable-notifications");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");
                options.addArguments("--no-default-browser-check");
                options.addArguments("--no-first-run");

                if (headless) {
                    options.addArguments("--headless=new");
                }

                // Create ChromeDriver instance
                ChromeDriver chrome = new ChromeDriver(options);
                driver.set(chrome);

                // CRITICAL FIX — FORCE Chrome to download without prompts, bypassing OneDrive entirely
                ((ChromiumDriver) chrome).executeCdpCommand(
                        "Page.setDownloadBehavior",
                        Map.of(
                                "behavior", "allow",
                                "downloadPath", downloadPath
                        )
                );

                break;

            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        // Maximize browser
        getDriver().manage().window().maximize();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            getDriver().quit();
            driver.remove();
        }
    }
}
