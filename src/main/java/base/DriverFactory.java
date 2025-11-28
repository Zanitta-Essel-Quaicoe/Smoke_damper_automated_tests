package base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import utils.PropertyReader;

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

        switch (browser) {

            case "chrome":
                WebDriverManager.chromedriver().setup();
                ChromeOptions options = new ChromeOptions();

                options.addArguments("--remote-allow-origins=*");
                options.addArguments("--disable-notifications");
                options.addArguments("--disable-gpu");
                options.addArguments("--no-sandbox");
                options.addArguments("--disable-dev-shm-usage");

                if (headless) {
                    options.addArguments("--headless=new");
                }

                driver.set(new ChromeDriver(options));
                break;

            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }

        getDriver().manage().window().maximize();
    }

    public static void quitDriver() {
        if (driver.get() != null) {
            getDriver().quit();
            driver.remove();
        }
    }
}
