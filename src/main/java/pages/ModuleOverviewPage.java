package pages;

import base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.UIHelpers;
import utils.WaitUtils;

import java.time.Duration;
import java.util.List;

public class ModuleOverviewPage extends BaseTest {

    private WebDriver driver;
    private WaitUtils wait;
    private UIHelpers ui;

    // -----------------------------
    // Constructors
    // -----------------------------

    /** DEFAULT constructor → used by MOST tests */
    public ModuleOverviewPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.ui = new UIHelpers(driver);
        verifyPageLoaded();
        ensureListView();
    }

    /** SPECIAL constructor → allows skipping list-view switching */
    public ModuleOverviewPage(WebDriver driver, boolean skipEnsureListView) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.ui = new UIHelpers(driver);
        verifyPageLoaded();
        if (!skipEnsureListView) {
            ensureListView();
        }
    }

    // -----------------------------
    // Locators
    // -----------------------------

    // LIST/GRID toggle
    private By listViewButton = By.cssSelector("app-list-grid-toggle button:nth-of-type(1)");
    private By gridViewButton = By.cssSelector("app-list-grid-toggle button:nth-of-type(2)");
    private By activeViewButton = By.cssSelector("app-list-grid-toggle button.active");

    // LIST VIEW table
    private By tableRows = By.cssSelector("app-module-manager-list-view table tbody tr");
    private By tableElement = By.cssSelector("app-module-manager-list-view table");

    // CORRECTED PAGINATION LOCATORS
    private By nextPageButton = By.cssSelector(".paginator_navigation app-button:last-of-type button");
    private By rangeText = By.cssSelector(".paginator_range_display");

    // FILTER button
    private By filterStatusDropdown = By.cssSelector("app-select[usecase='module_status_filter'] button.select");

    // Overlay items
    private By filterOptionsOverlay = By.cssSelector(".cdk-overlay-pane app-action-items-overlay ul li button");

    // MODULE IP column
    private By moduleIpCells = By.cssSelector("td.mat-column-module_id");

    //Network Status column
    private By networkStatusCells = By.cssSelector("td.mat-column-network_status app-module-status-indicator");

    // I/O Status column
    private By ioStatusColumn = By.cssSelector("td.mat-column-io_status app-module-status-indicator");

    private By ioStatusCells = By.cssSelector("app-module-manager-list-view table tbody tr td:nth-child(5)");

    private By noDataMessage = By.xpath("//app-no-data//p[contains(text(),'No matching modules')]");
    private By resetFilterButton = By.xpath("//button[.//span[text()='Reset']]");

    // EXPORT BUTTON
    private By exportButton = By.xpath("//div[contains(@class,'exports--module_commissioning')]//span[contains(text(),'Export')]");



    // -----------------------------
    // Page Load Verification
    // -----------------------------
    private void verifyPageLoaded() {
        try {
            WebDriverWait strongWait = new WebDriverWait(driver, Duration.ofSeconds(25));

            strongWait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(listViewButton),
                    ExpectedConditions.visibilityOfElementLocated(gridViewButton)
            ));

            System.out.println("✔ Module Overview loaded.");
        } catch (TimeoutException e) {
            System.out.println(" Module Overview FAILED to load.");
            throw e;
        }
    }


    // -----------------------------
    // Ensure LIST VIEW
    // -----------------------------
    private void ensureListView() {
        try {
            WebElement active = driver.findElement(activeViewButton);

            String activeImg = active.findElement(By.tagName("img")).getAttribute("alt");

            if (activeImg.toLowerCase().contains("list")) {
                System.out.println("✔ Already in LIST view.");
                waitForTableToLoad();
                return;
            }

            System.out.println("➡ Switching to LIST view...");
            ui.safeClick(listViewButton);

            waitForTableToLoad();
            System.out.println("✔ Switched to LIST view successfully.");

        } catch (Exception e) {
            System.out.println("⚠ Could not determine active view. Trying LIST view click anyway.");
            ui.safeClick(listViewButton);
            waitForTableToLoad();
        }
    }


    // -----------------------------
    // Wait for table to load
    // -----------------------------
    private void waitForTableToLoad() {
        WebDriverWait strongWait = new WebDriverWait(driver, Duration.ofSeconds(40));

        strongWait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(tableRows),
                ExpectedConditions.visibilityOfElementLocated(noDataMessage)
        ));

        wait.waitForSeconds(1);
    }


    // ------------------------------------------------
    // Set rows per page = 100
    // ------------------------------------------------
    public void setRowsPerPageTo100() {
        try {
            WebElement dropdown = wait.waitForClickable(By.id("rows-per-page"));
            dropdown.click();
            dropdown.sendKeys("100");
            dropdown.sendKeys(Keys.ENTER);

            wait.waitForSeconds(1);

            System.out.println("✔ rows-per-page set to 100.");
        } catch (Exception e) {
            System.out.println("⚠ Could not set rows-per-page to 100. Using default.");
        }
    }


    // ------------------------------------------------
    // Count ALL modules across ALL pages
    // ------------------------------------------------
    public int countAllModules() {
        System.out.println("\n=== COUNTING MODULES IN MODULE OVERVIEW ===");

        ensureListView();
        setRowsPerPageTo100();
        wait.waitForSeconds(1);

        int total = countRowsOnCurrentPage();
        System.out.println(" Page 1 rows = " + total);

        // Loop through pagination
        while (hasNextPage()) {
            goToNextPage();
            int rows = countRowsOnCurrentPage();
            System.out.println(" Next page rows = " + rows);
            total += rows;
        }

        System.out.println("✔ TOTAL modules counted = " + total);
        return total;
    }


    private int countRowsOnCurrentPage() {
        waitForTableToLoad();
        List<WebElement> rows = driver.findElements(tableRows);
        return rows.size();
    }


    // detect if next button is enabled
    private boolean hasNextPage() {
        try {
            WebElement next = driver.findElement(nextPageButton);
            return next.isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }


    // click next page reliably
    private void goToNextPage() {
        System.out.println("➡ Clicking NEXT PAGE...");
        ui.safeClick(nextPageButton);
        wait.waitForSeconds(2);
    }

    public void applyStatusFilter(String status) {
        System.out.println("➡ Applying Status Filter: " + status);

        // Open dropdown
        ui.safeClick(filterStatusDropdown);

        wait.waitForSeconds(1);

        // Wait for overlay and find matching button
        List<WebElement> options = wait.waitForVisibleElements(filterOptionsOverlay);

        boolean found = false;

        for (WebElement opt : options) {
            if (opt.getText().trim().equalsIgnoreCase(status)) {
                opt.click();
                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException(" Filter option not found: " + status);
        }

        wait.waitForSeconds(2); // allow table refresh
        waitForTableToLoad();
    }

    private boolean isNoMatchingModulesMessageVisible() {
        try {
            return driver.findElement(noDataMessage).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean validateStatusAcrossAllPages(String expectedStatus) {

        expectedStatus = expectedStatus.trim().toLowerCase();

        ensureListView();
        setRowsPerPageTo100();
        wait.waitForSeconds(1);

        waitForTableToLoad();

        // FIRST CHECK: Is "No matching modules found" visible?
        if (isNoMatchingModulesMessageVisible()) {
            System.out.println("✔ No matching modules found for filter: " + expectedStatus);
            return true;
        }

        int page = 1;

        while (true) {
            System.out.println("➡ Validating page " + page);

            waitForTableToLoad();

            List<WebElement> statuses = driver.findElements(ioStatusCells);

            for (WebElement statusCell : statuses) {
                String actual = statusCell.getText().trim().toLowerCase();

                if (!actual.contains(expectedStatus)) {
                    System.out.println(" MISMATCH: expected = " + expectedStatus + ", actual = " + actual);
                    return false;
                }
            }

            if (!hasNextPage()) break;

            goToNextPage();
            page++;
        }

        System.out.println("✔ All pages validated for filter: " + expectedStatus);
        return true;
    }

    public void resetFilters() {
        try {
            System.out.println("➡ Clicking RESET filter...");
            ui.safeClick(resetFilterButton);
            wait.waitForSeconds(2);
            waitForTableToLoad();
            System.out.println("✔ Filters reset successfully.");
        } catch (Exception e) {
            System.out.println(" Failed to click Reset filter.");
            throw e;
        }
    }

    public boolean validateResetShowsAllModules() {

        waitForTableToLoad();

        // If reset worked, table should NOT show the 'no data' message
        if (isNoMatchingModulesMessageVisible()) {
            System.out.println(" Reset failed: No data message still displayed!");
            return false;
        }

        // Ensure at least 1 row exists
        int countPage1 = countRowsOnCurrentPage();
        if (countPage1 == 0) {
            System.out.println(" Reset failed: Table is empty after reset.");
            return false;
        }

        System.out.println("✔ Reset shows modules on page 1. Proceeding to full count...");

        // Count ALL modules across pages (same as before)
        int totalAfterReset = countAllModules();

        System.out.println("✔ Total modules after reset = " + totalAfterReset);

        return totalAfterReset > 0;
    }

    // Click Export
    public void clickExport() {
        try {
            System.out.println("➡ Clicking Export on Module Overview...");

            wait.waitForSectionToRender(By.xpath("//div[contains(@class,'exports--module_commissioning')]"));

            WebElement btn = wait.waitForClickable(exportButton);

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", btn);
            wait.waitForSeconds(1);

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn);

            System.out.println("✔ Export triggered.");

        } catch (Exception e) {
            System.out.println(" Failed to click Export: " + e.getMessage());
            throw e;
        }
    }

    // -----------------------------
    // Module IP Verification
    // -----------------------------
    public List<String> getAllModuleIPs() {

        ensureListView();
        setRowsPerPageTo100();
        wait.waitForSeconds(1);

        waitForTableToLoad();

        List<String> ipList = new java.util.ArrayList<>();

        int page = 1;

        while (true) {
            System.out.println("➡ Collecting Module IPs from page " + page);

            waitForTableToLoad();

            List<WebElement> cells = driver.findElements(moduleIpCells);

            for (WebElement cell : cells) {
                String ip = cell.getText().trim();
                ipList.add(ip);
            }

            if (!hasNextPage()) break;

            goToNextPage();
            page++;
        }

        System.out.println("✔ TOTAL collected Module IP values = " + ipList.size());
        return ipList;
    }

    public boolean validateModuleIPs() {

        List<String> ips = getAllModuleIPs();

        java.util.Set<String> uniqueCheck = new java.util.HashSet<>();

        for (String ip : ips) {

            // 1. Check for "unknown"
            if (ip.equalsIgnoreCase("unknown") || ip.isBlank()) {
                System.out.println(" Invalid Module IP found: '" + ip + "'");
                return false;
            }

            // 2. Validate numeric format
            if (!ip.matches("\\d+")) {
                System.out.println(" Non-numeric Module IP found: " + ip);
                return false;
            }

            // 3. Validate 4-digit pattern
            if (ip.length() != 4) {
                System.out.println(" Module IP not 4 digits: " + ip);
                return false;
            }

            // 4. Check for duplicates
            if (!uniqueCheck.add(ip)) {
                System.out.println(" Duplicate Module IP detected: " + ip);
                return false;
            }
        }

        System.out.println("✔ All Module IPs are valid, unique, and correctly formatted.");
        return true;
    }

    // -----------------------------
    // Network Status Verification
    // -----------------------------
    public boolean validateNetworkStatus() {

        System.out.println("\n=== VALIDATING NETWORK STATUS COLUMN ===");

        ensureListView();
        setRowsPerPageTo100();
        waitForTableToLoad();

        int page = 1;

        while (true) {
            System.out.println("➡ Validating page " + page);

            List<WebElement> indicators = driver.findElements(networkStatusCells);

            if (indicators.isEmpty()) {
                System.out.println("⚠ No network status indicators found on page " + page);
                return false;
            }

            for (WebElement indicator : indicators) {

                String text = indicator.getText().trim().toLowerCase();

                String iconSrc = "";
                try {
                    WebElement img = indicator.findElement(By.tagName("img"));
                    iconSrc = img.getAttribute("src");
                } catch (Exception ignored) {}

                // FIX HERE: get class from the INNER DIV
                String classes = "";
                try {
                    WebElement statusDiv = indicator.findElement(By.cssSelector("div.module_status_indicator"));
                    classes = statusDiv.getAttribute("class");
                } catch (Exception e) {
                    System.out.println(" Could not extract internal status div class.");
                    return false;
                }

                System.out.println(" → Found status: " + text + " | classes: " + classes + " | icon: " + iconSrc);

                switch (text) {
                    case "online":
                        if (!classes.contains("healthy")) return false;
                        if (!iconSrc.contains("online")) return false;
                        break;

                    case "offline":
                        if (!classes.contains("faulty")) return false;
                        if (!iconSrc.contains("offline")) return false;
                        break;

                    case "pending":
                        if (!classes.contains("pending")) return false;
                        if (!iconSrc.contains("pending")) return false;
                        break;

                    default:
                        System.out.println(" INVALID NETWORK STATUS FOUND: " + text);
                        return false;
                }
            }

            if (!hasNextPage()) break;

            goToNextPage();
            page++;
        }

        System.out.println("✔ All Network Status values validated successfully.");
        return true;
    }

    // -----------------------------
    // Network Status Verification
    // -----------------------------
    public boolean validateIOStatusAcrossPages() {

        System.out.println("\n=== VALIDATING I/O STATUS COLUMN ===");

        ensureListView();
        setRowsPerPageTo100();
        waitForTableToLoad();

        int page = 1;

        while (true) {
            System.out.println("➡ Validating page " + page);

            List<WebElement> indicators = driver.findElements(ioStatusColumn);

            for (WebElement indicator : indicators) {

                // TEXT (Healthy / Pending / Faulty)
                String text = indicator.getText().trim().toLowerCase();

                // ICON
                String iconSrc = "";
                try {
                    WebElement img = indicator.findElement(By.tagName("img"));
                    iconSrc = img.getAttribute("src");
                } catch (Exception ignored) {}

                // INNER DIV CLASS
                String classes = "";
                try {
                    WebElement statusDiv =
                            indicator.findElement(By.cssSelector("div.module_status_indicator"));
                    classes = statusDiv.getAttribute("class");
                } catch (Exception e) {
                    System.out.println(" Could not extract I/O status class.");
                    return false;
                }

                System.out.println(" → Found I/O status: " + text +
                        " | classes: " + classes +
                        " | icon: " + iconSrc);

                switch (text) {
                    case "healthy":
                        if (!classes.contains("healthy")) return false;
                        if (!iconSrc.contains("healthy")) return false;
                        break;

                    case "pending":
                        if (!classes.contains("pending")) return false;
                        if (!iconSrc.contains("pending")) return false;
                        break;

                    case "faulty":
                        if (!classes.contains("faulty")) return false;
                        if (!iconSrc.contains("faulty")) return false;
                        break;

                    default:
                        System.out.println(" INVALID I/O STATUS FOUND: " + text);
                        return false;
                }
            }

            if (!hasNextPage()) break;

            goToNextPage();
            page++;
        }

        System.out.println("✔ I/O Status validated across all pages.");
        return true;
    }



}
