package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.UIHelpers;
import utils.WaitUtils;

public class SidebarPage {

    private WebDriver driver;
    private WaitUtils wait;
    private UIHelpers ui;

    // === Locators ===
    private By dashboardMenu = By.xpath("//span[@class='menu_item_name' and text()='Dashboard']");
    private By moduleOverviewMenu = By.xpath("//span[@class='menu_item_name' and text()='Module Overview']");
    private By eventLogsMenu = By.xpath("//span[@class='menu_item_name' and text()='Event Logs']");
    private By alertsMenu = By.xpath("//span[@class='menu_item_name' and text()='Alerts']");
    private By systemConfigMenu = By.xpath("//span[@class='menu_item_name' and text()='System Configuration']");

    // === Constructor ===
    public SidebarPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.ui = new UIHelpers(driver);
    }

    // === Navigation Methods ===

    public DashboardPage goToDashboard() {
        ui.safeClick(dashboardMenu);
        wait.waitForUrlContains("/dashboard");
        System.out.println("➡ Navigated to Dashboard");
        return new DashboardPage(driver);
    }

    public ModuleOverviewPage goToModuleOverview() {
        ui.safeClick(moduleOverviewMenu);
        wait.waitForUrlContains("/module-overview");
        System.out.println("➡ Navigated to Module Overview");
        return new ModuleOverviewPage(driver, false);
    }

    public EventLogsPage goToEventLogs() {
        ui.safeClick(eventLogsMenu);
        wait.waitForUrlContains("/event-logs");
        System.out.println("➡ Navigated to Event Logs");
        return new EventLogsPage(driver);
    }

    public AlertsPage goToAlerts() {
        ui.safeClick(alertsMenu);
        wait.waitForUrlContains("/alerts");
        System.out.println("➡ Navigated to Alerts");
        return new AlertsPage(driver);
    }

    public SystemConfigurationPage goToSystemConfig() {
        ui.safeClick(systemConfigMenu);
        wait.waitForUrlContains("/system-config");
        System.out.println("➡ Navigated to System Configuration");
        return new SystemConfigurationPage(driver);
    }
}
