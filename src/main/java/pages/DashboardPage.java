package pages;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.UIHelpers;
import utils.WaitUtils;

import java.time.Duration;

public class DashboardPage extends BaseTest {

    private WebDriver driver;
    private WaitUtils wait;
    private UIHelpers ui;

    // === Locators ===
    private By viewAllModulesButton = By.xpath("//div[contains(@class,'sample_modules')]//app-button//span[contains(text(),'View all')]");
    private By viewAllEventsButton = By.xpath("//div[contains(@class,'latest_event_logs')]//app-button//span[contains(text(),'View all')]");
    private By viewDeviceListButton = By.xpath("//button[.//span[text()='View device list']]");
    private By dashboardMainHeader = By.xpath("//h1[contains(text(),'Dashboard')]");
    private By totalModulesValue = By.xpath( "//app-dashboard-summary-card[@title='Total modules']//div[@class='data']/div[@class='total']");

    // === Constructor ===
    public DashboardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.ui = new UIHelpers(driver);

        verifyDashboardLoaded();
    }

    // === Page Verification ===
    private void verifyDashboardLoaded() {
        try {
            wait.waitForVisible(dashboardMainHeader);
            System.out.println("✔ Dashboard page loaded successfully.");
        } catch (Exception e) {
            System.out.println(" ERROR: Dashboard page did not load properly. Header not found.");
            throw e;
        }
    }

    // === Navigation Methods ===

    /** Navigate to Module Overview */
    public ModuleOverviewPage clickViewAllModules() {
        try {
            System.out.println("➡ Clicking 'View all' under Module Overview...");

            wait.waitForSectionToRender(
                    By.xpath("//div[contains(@class,'sample_modules')]")
            );

            WebElement btn = wait.waitForClickable(viewAllModulesButton);

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            wait.waitForSeconds(1);

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

            wait.waitForUrlContains("/module-overview");

            System.out.println("✔ Navigated to Module Overview page (via View all).");

            return new ModuleOverviewPage(driver);

        } catch (Exception e) {
            System.out.println(" Failed to click 'View all' for Module Overview: " + e.getMessage());
            throw e;
        }
    }

    /** Read Total Modules  */
    public int getTotalModulesCount() {
        try {
            String countText = wait.waitForVisible(totalModulesValue).getText().trim();
            System.out.println(" Dashboard Total Modules = " + countText);
            return Integer.parseInt(countText);
        } catch (Exception e) {
            System.out.println(" ERROR: Couldn't read Total Modules value.");
            throw e;
        }
    }

    /** Navigate to Event Logs */
    public EventLogsPage clickViewAllEventLogs() {
        try {
            ui.safeClick(viewAllEventsButton);
            System.out.println("➡ Navigating to Event Logs page...");
            return new EventLogsPage(driver);
        } catch (Exception e) {
            System.out.println(" Failed to click 'View all' (Events). Element missing or not clickable.");
            throw e;
        }
    }

    /** Navigate to Module Overview (List view version) */
    public ModuleOverviewPage clickViewDeviceList() {
        try {
            System.out.println("➡ Clicking 'View device list' …");

            wait.waitForSectionToRender(
                    By.xpath("//div[contains(@class,'summary_card')]//h3[contains(text(),'Total modules')]")
            );

            WebElement btn = wait.waitForClickable(viewDeviceListButton);

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            wait.waitForSeconds(1);

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

            wait.waitForUrlContains("/module-overview");

            System.out.println("✔ Navigated to Module Overview page via 'View device list'.");

            return new ModuleOverviewPage(driver);

        } catch (Exception e) {
            System.out.println(" Failed to navigate via 'View device list': " + e.getMessage());
            throw e;
        }
    }

    // === Useful Check Methods ===
    public boolean isDashboardHeaderVisible() {
        try {
            return driver.findElement(dashboardMainHeader).isDisplayed();
        } catch (Exception e) {
            System.out.println("⚠ Warning: Dashboard header is not visible.");
            return false;
        }
    }
}
