package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.WaitUtils;

public class EventLogsPage {

    private WebDriver driver;
    private WaitUtils wait;

    public EventLogsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
    }

    // =============================
    // Locators
    // =============================

    private By pageHeader = By.xpath("//h2[contains(text(),'Event Logs Overview')]");
    private By searchBox = By.xpath("//input[@placeholder='Search module name or ip']");
    private By exportButton = By.xpath("//button[contains(@class,'export') or contains(text(),'Export')]");

    // Table Locators
    private By tableRows = By.xpath("//table//tbody//tr");
    private By firstRow = By.xpath("(//table//tbody//tr)[1]");

    // Filters
    private By filterToday = By.xpath("//button[contains(.,'Today')]");
    private By resetFilters = By.xpath("//button[contains(text(),'Reset all')]");

    // =============================
    // Constructor
    // =============================

    public EventLogsPage(WebDriver driver, WaitUtils wait) {
        this.driver = driver;
        this.wait = wait;
        verifyEventLogsPage();
    }

    // =============================
    // Page Verification
    // =============================

    private void verifyEventLogsPage() {
        try {
            wait.waitForVisible(pageHeader);
            System.out.println("✔ Event Logs page opened successfully.");
        } catch (Exception e) {
            System.out.println(" ERROR: Event Logs page did NOT load correctly.");
            throw e;
        }
    }

    // =============================
    // Actions
    // =============================

    public void searchModule(String text) {
        wait.waitForVisible(searchBox).sendKeys(text);
        System.out.println("✔ Typed in search box: " + text);
    }

    public void clickExport() {
        wait.waitForClickable(exportButton).click();
        System.out.println("✔ Export button clicked.");
    }

    public void clickTodayFilter() {
        wait.waitForClickable(filterToday).click();
        System.out.println("✔ Today filter clicked.");
    }

    public void clickResetAll() {
        wait.waitForClickable(resetFilters).click();
        System.out.println("✔ Reset all filters clicked.");
    }

    public int getEventCount() {
        wait.waitForVisible(firstRow); // ensures table is loaded
        int count = driver.findElements(tableRows).size();
        System.out.println("✔ Number of event log entries: " + count);
        return count;
    }

}
