package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.UIHelpers;
import utils.WaitUtils;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class AlertsPage {

    private WebDriver driver;
    private WaitUtils wait;
    private UIHelpers ui;

    // ============================
    // Locators
    // ============================
    private By pageHeader = By.xpath("//h1[contains(text(),'Alerts')]");

    private By alertsTable = By.cssSelector("app-alerts table");
    private By alertRows = By.cssSelector("app-alerts table tbody tr");

    // Optional buttons (if available)
    private By exportButton = By.xpath("//button[.//span[text()='Export']]");

    // ============================
    // Constructor
    // ============================
    public AlertsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.ui = new UIHelpers(driver);

        verifyPageLoaded();
    }

    // ============================
    // Verify the Alerts Page Loaded
    // ============================
    private void verifyPageLoaded() {
        try {
            WebDriverWait strongWait = new WebDriverWait(driver, Duration.ofSeconds(20));

            strongWait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(pageHeader),
                    ExpectedConditions.visibilityOfElementLocated(alertsTable)
            ));

            System.out.println("✔ Alerts page loaded successfully.");

        } catch (Exception e) {
            System.out.println(" FAILED: Alerts page did NOT load properly.");
            throw e;
        }
    }

    // ============================
    // Basic Table Accessors
    // ============================
    public int getAlertsCount() {
        return driver.findElements(alertRows).size();
    }

    public boolean isExportButtonVisible() {
        try {
            return driver.findElement(exportButton).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
