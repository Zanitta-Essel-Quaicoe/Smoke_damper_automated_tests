package pages;

import base.BaseTest;
import org.openqa.selenium.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.UIHelpers;
import utils.WaitUtils;

import java.time.Duration;
import java.util.*;

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

    //Filters
    //Network Status filters
    private By networkFilterButton = By.cssSelector("app-select[usecase='module_status_filter'] button.select");
    private By networkFilterOptions = By.cssSelector(".cdk-overlay-pane ul li button");

    //I/O Status filters
    private By ioFilterButton = By.xpath("//button[.//div[text()='I/O Status']]");
    private By ioFilterOptions = By.cssSelector(".cdk-overlay-pane ul li button");

    //All filters
    private By allFilterButton = By.xpath("//button[.//div[text()='All']]");
    private By allFilterOptions = By.cssSelector(".cdk-overlay-pane ul li button");

    //Reset button
    private By resetButton = By.cssSelector("app-button[text='Reset'] button");

    // EXPORT BUTTON
    private By exportButton = By.xpath("//div[contains(@class,'exports--module_commissioning')]//span[contains(text(),'Export')]");

    // MODULE IP column
    private By moduleIpCells = By.cssSelector("td.mat-column-module_id");

    //Network Status column
    private By networkStatusCells = By.cssSelector("td.mat-column-network_status app-module-status-indicator");

    // I/O Status column
    private By ioStatusColumn = By.cssSelector("td.mat-column-io_status app-module-status-indicator");

    private By noDataMessage = By.xpath("//app-no-data//p[contains(text(),'No matching modules')]");

    //Last seen column
    private By lastSeenCells = By.cssSelector("app-module-manager-list-view table tbody tr td.cdk-column-last_seen");

    // ACTION COLUMN
    private By actionButtons = By.cssSelector("td.cdk-column-action button.khebab_action");

    // Overlay menu items
    private By actionOverlayItems = By.cssSelector(".cdk-overlay-pane app-action-items-overlay ul li button");

    // Module name cell (to detect commissioned/uncommissioned)
    private By moduleNameCell = By.cssSelector("td.cdk-column-module_name span");


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
            WebElement active = safeFind(activeViewButton);

            String activeImg = safeFindWithin(active, By.tagName("img")).getAttribute("alt");

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
        List<WebElement> rows = safeFindAll(tableRows);
        return rows.size();
    }


    // detect if next button is enabled
    private boolean hasNextPage() {
        try {
            WebElement next = safeFind(nextPageButton);
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

    // -----------------------------
    // === GLOBAL SAFE HELPERS ===
    // -----------------------------
    /**
     * safeFind: find single element with retries to avoid stale exceptions
     */
    private WebElement safeFind(By locator) {
        int attempts = 0;
        while (attempts < 4) {
            try {
                WebElement el = driver.findElement(locator);
                return el;
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                attempts++;
                wait.waitForSeconds(1);
            }
        }
        // final attempt using explicit wait
        try {
            WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(5));
            return w.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            throw new NoSuchElementException("Element not found: " + locator.toString());
        }
    }

    /**
     * safeFindAll: find elements with a short retry loop; returns empty list if none
     */
    private List<WebElement> safeFindAll(By locator) {
        int attempts = 0;
        while (attempts < 4) {
            try {
                List<WebElement> els = driver.findElements(locator);
                return els == null ? new ArrayList<>() : els;
            } catch (StaleElementReferenceException e) {
                attempts++;
                wait.waitForSeconds(1);
            }
        }
        // last resort
        try {
            return driver.findElements(locator);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * safeFindWithin: find child element within parent with retries
     */
    private WebElement safeFindWithin(WebElement parent, By childLocator) {
        int attempts = 0;
        while (attempts < 4) {
            try {
                return parent.findElement(childLocator);
            } catch (StaleElementReferenceException | NoSuchElementException e) {
                attempts++;
                wait.waitForSeconds(1);
                // refresh parent if possible (best-effort)
                try {
                    // no-op: allow loop to retry
                } catch (Exception ignored) {}
            }
        }
        throw new NoSuchElementException("Child element not found: " + childLocator.toString());
    }

    /**
     * safeGetText: robust getText with retries against stale elements
     */
    private String safeGetText(WebElement el) {
        int attempts = 0;
        while (attempts < 4) {
            try {
                return el.getText();
            } catch (StaleElementReferenceException e) {
                attempts++;
                wait.waitForSeconds(1);
            }
        }
        return "";
    }

    /**
     * safeClick on WebElement with retry and fallback to JS click
     */
    private void safeClickElement(WebElement el) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                el.click();
                return;
            } catch (ElementClickInterceptedException | StaleElementReferenceException e) {
                attempts++;
                wait.waitForSeconds(1);
                try {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", el);
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
                    return;
                } catch (Exception ignored) {}
            } catch (Exception e) {
                attempts++;
                wait.waitForSeconds(1);
            }
        }
        // final attempt by JS (best-effort)
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
        } catch (Exception e) {
            throw new RuntimeException("Could not click element: " + e.getMessage());
        }
    }

    /**
     * safeClick using a locator
     */
    private void safeClick(By locator) {
        WebElement el = safeFind(locator);
        safeClickElement(el);
    }

    // =========================
    // Network Status Filter
    // =========================
    public void applyNetworkFilter(String value) {
        System.out.println("➡ Applying Network Status Filter: " + value);

        // Open dropdown (use safeClick wrapper)
        safeClick(networkFilterButton);
        wait.waitForSeconds(1);

        // Wait for overlay options
        List<WebElement> options = safeFindAll(networkFilterOptions);

        boolean found = false;

        for (WebElement opt : options) {
            String txt = safeGetText(opt).trim();
            if (txt.equalsIgnoreCase(value)) {
                safeClickElement(opt);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException(" Network Status filter option NOT FOUND: " + value);
        }

        wait.waitForSeconds(2);
        waitForTableToLoad();

        System.out.println("✔ Network Status filter applied: " + value);
    }

    private String mapNetworkStatus(String filter) {
        switch (filter.toLowerCase()) {
            case "connected": return "online";
            case "disconnected": return "offline";
            case "pending": return "pending";
            case "degraded": return "degraded";
            default: return "";
        }
    }


    public boolean validateNetworkFilterResults(String filterValue) {

        System.out.println("\n=== VALIDATING NETWORK FILTER RESULTS FOR: " + filterValue + " ===");

        String expectedStatus = mapNetworkStatus(filterValue).toLowerCase();

        ensureListView();
        setRowsPerPageTo100();
        waitForTableToLoad();

        int page = 1;
        int totalMatched = 0;

        while (true) {

            System.out.println("➡ Validating page " + page);
            waitForTableToLoad();

            List<WebElement> statuses = safeFindAll(By.cssSelector("td.mat-column-network_status"));

            if (statuses.isEmpty()) {
                System.out.println("⚠ No rows found on page " + page);
            }

            int pageMatched = 0;

            for (WebElement cell : statuses) {
                String text = safeGetText(cell).trim().toLowerCase();

                if (text.contains(expectedStatus)) {
                    pageMatched++;
                } else {
                    System.out.println(" MISMATCH → Expected: " + expectedStatus + ", Found: " + text);
                    return false;
                }
            }

            System.out.println(" Page " + page + " matched rows = " + pageMatched);

            totalMatched += pageMatched;

            if (!hasNextPage()) break;

            goToNextPage();
            page++;
        }

        // FINAL SUMMARY LOGGING
        if (totalMatched == 0) {
            System.out.println("⚠ WARNING: No modules found with Network Status = " + expectedStatus);
            return true;
        }

        System.out.println("Found " + totalMatched + " modules with Network Status = " + expectedStatus);
        System.out.println("✔ All rows match expected Network Status: " + expectedStatus);

        return true;
    }


    // =========================
    // I/O Status Filter
    // =========================
    public void applyIOFilter(String value) {

        System.out.println("➡ Applying I/O Status Filter: " + value);

        safeClick(ioFilterButton);
        wait.waitForSeconds(1);

        List<WebElement> options = safeFindAll(ioFilterOptions);

        boolean found = false;

        for (WebElement opt : options) {
            String txt = safeGetText(opt).trim();
            if (txt.equalsIgnoreCase(value)) {
                safeClickElement(opt);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new RuntimeException(" I/O Status filter option NOT FOUND: " + value);
        }

        wait.waitForSeconds(2);
        waitForTableToLoad();

        System.out.println("✔ I/O Status filter applied: " + value);
    }

    public boolean validateIOFilterResults(String filterValue) {

        System.out.println("\n=== VALIDATING I/O FILTER RESULTS FOR: " + filterValue + " ===");

        String expected = filterValue.toLowerCase();
        int totalRowsChecked = 0;
        int matchedCount = 0;

        ensureListView();
        setRowsPerPageTo100();
        waitForTableToLoad();

        int page = 1;

        while (true) {

            System.out.println("➡ Validating page " + page);

            List<WebElement> statuses = safeFindAll(ioStatusColumn);

            if (statuses.isEmpty()) {
                System.out.println("⚠ No rows found on page " + page);
            }

            for (WebElement cell : statuses) {
                totalRowsChecked++;

                String text = safeGetText(cell).trim().toLowerCase();

                if (text.contains(expected)) {
                    matchedCount++;
                } else {
                    System.out.println(
                            " MISMATCH → Expected: " + expected + " | Found: " + text
                    );
                    return false;
                }
            }

            System.out.println(" Page " + page + " matched rows = " + matchedCount);

            if (!hasNextPage()) break;

            goToNextPage();
            page++;
        }

        // ---- Logging summary ----
        if (matchedCount == 0) {
            System.out.println("⚠ WARNING: No modules found with I/O Status = " + expected);
            return true;   // Do NOT print success message
        }

        System.out.println("Found " + matchedCount + " modules with I/O Status = " + expected);
        System.out.println("✔ All rows match expected I/O Status: " + expected);
        return true;

    }

    // =============================================
    // APPLY "ALL" FILTER (All / Commissioned / Uncommissioned)
    // =============================================
    public void applyAllFilter(String value) {

        System.out.println("\n➡ Applying ALL filter: " + value);

        // Locate ONLY the All dropdown (third app-select)
        By allDropdown = By.cssSelector("app-select[usecase='module_status_filter']:nth-of-type(3) button.select");

        safeClick(allDropdown);
        wait.waitForSeconds(1);

        // Overlay items
        By options = By.cssSelector(".cdk-overlay-pane app-action-items-overlay ul li button");

        List<WebElement> items = safeFindAll(options);

        for (WebElement item : items) {
            String txt = safeGetText(item).trim();
            if (txt.equalsIgnoreCase(value)) {
                safeClickElement(item);
                wait.waitForSeconds(2);
                waitForTableToLoad();
                return;
            }
        }

        throw new RuntimeException(" Could not find All filter option: " + value);
    }

    private String getCommissionState(WebElement row) {
        try {
            WebElement span = safeFindWithin(row, By.cssSelector("td.cdk-column-module_name span"));
            String clazz = span.getAttribute("class").trim().toLowerCase();

            if (clazz.equals("na")) return "commissioned";
            if (clazz.equals("uncommissioned")) return "uncommissioned";

        } catch (Exception ignored) {}

        return "unknown";
    }

    // =============================================
    // VALIDATE RESULTS FOR: All / Commissioned / Uncommissioned
    // =============================================
    public boolean validateAllFilterResults(String filterValue) {

        System.out.println("\n=== VALIDATING ALL FILTER RESULTS FOR: " + filterValue + " ===");

        ensureListView();
        setRowsPerPageTo100();
        waitForTableToLoad();

        int page = 1;
        int rowCount = 0;

        while (true) {

            System.out.println("➡ Validating page " + page);

            List<WebElement> rows = safeFindAll(By.cssSelector("app-module-manager-list-view table tbody tr"));

            for (WebElement row : rows) {

                String state = getCommissionState(row);
                rowCount++;

                switch (filterValue.toLowerCase()) {

                    case "all":
                        break;

                    case "commissioned":
                        if (!state.equals("commissioned")) {
                            System.out.println(" MISMATCH → Expected commissioned but found: " + state);
                            return false;
                        }
                        break;

                    case "uncommissioned":
                        if (!state.equals("uncommissioned")) {
                            System.out.println(" MISMATCH → Expected uncommissioned but found: " + state);
                            return false;
                        }
                        break;

                    default:
                        System.out.println(" Invalid filter type: " + filterValue);
                        return false;
                }
            }

            if (!hasNextPage()) break;

            goToNextPage();
            page++;
        }

        // Logging
        if (rowCount == 0) {
            System.out.println("⚠ WARNING: No modules found for filter = " + filterValue);
        } else {
            System.out.println("✔ Total matching modules for '" + filterValue + "': " + rowCount);
        }

        System.out.println("✔ All rows match expected filter: " + filterValue);
        return true;
    }

    // =========================
// Reset Filters
// =========================
    public boolean resetFiltersNew() {
        System.out.println("\n➡ Clicking RESET filter...");

        try {
            WebElement reset = safeFind(resetButton);
            safeClickElement(reset);
            wait.waitForSeconds(2);
            waitForTableToLoad();

            System.out.println("✔ Reset clicked successfully.");
        } catch (Exception e) {
            System.out.println(" Could not click Reset button: " + e.getMessage());
            return false;
        }

        return validateFiltersCleared();
    }


    // =========================
    // Validate filters cleared
    // =========================
    private boolean validateFiltersCleared() {

        System.out.println("➡ Validating that filters were cleared...");

        List<WebElement> dropdownLabels =
                safeFindAll(By.cssSelector("app-select[usecase='module_status_filter'] button.select .label"));

        if (dropdownLabels.size() < 3) {
            System.out.println(" Could not locate all dropdown labels.");
            return false;
        }

        String networkLabel = safeGetText(dropdownLabels.get(0));
        String ioLabel = safeGetText(dropdownLabels.get(1));
        String allLabel = safeGetText(dropdownLabels.get(2));

        System.out.println(" → Network Status label: " + networkLabel);
        System.out.println(" → I/O Status label: " + ioLabel);
        System.out.println(" → All label: " + allLabel);

        boolean ok =
                networkLabel.equalsIgnoreCase("Network Status") &&
                        ioLabel.equalsIgnoreCase("I/O Status") &&
                        allLabel.equalsIgnoreCase("All");

        if (!ok) {
            System.out.println(" One or more filters did NOT reset properly.");
            return false;
        }

        System.out.println("✔ All filter dropdowns reset successfully.");
        return true;
    }


    // =========================
    // Click Export
    // =========================
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

    // =========================
    // Validate Module name column
    // =========================
    public boolean validateModuleNames() {

        System.out.println("\n=== VALIDATING MODULE NAME COLUMN ===");

        ensureListView();
        setRowsPerPageTo100();
        waitForTableToLoad();

        Set<String> uniqueNames = new HashSet<>();
        int page = 1;

        By moduleNameCells = By.cssSelector("app-module-manager-list-view table tbody tr td.cdk-column-module_name");

        while (true) {
            System.out.println("➡ Validating page " + page);

            waitForTableToLoad();

            List<WebElement> names = safeFindAll(moduleNameCells);

            for (WebElement cell : names) {
                String name = safeGetText(cell).trim();

                if (name.isEmpty()) {
                    System.out.println(" Empty module name found!");
                    return false;
                }

                if (!uniqueNames.add(name)) {
                    System.out.println(" Duplicate module name found: " + name);
                    return false;
                }
            }

            if (!hasNextPage()) break;

            goToNextPage();
            page++;
        }

        System.out.println("✔ All module names validated successfully — no empty values, no duplicates.");
        return true;
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

            List<WebElement> cells = safeFindAll(moduleIpCells);

            for (WebElement cell : cells) {
                String ip = safeGetText(cell).trim();
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

            List<WebElement> indicators = safeFindAll(networkStatusCells);

            if (indicators.isEmpty()) {
                System.out.println("⚠ No network status indicators found on page " + page);
                return false;
            }

            for (WebElement indicator : indicators) {

                String text = safeGetText(indicator).trim().toLowerCase();

                String iconSrc = "";
                try {
                    WebElement img = safeFindWithin(indicator, By.tagName("img"));
                    iconSrc = img.getAttribute("src");
                } catch (Exception ignored) {}

                // FIX HERE: get class from the INNER DIV
                String classes = "";
                try {
                    WebElement statusDiv = safeFindWithin(indicator, By.cssSelector("div.module_status_indicator"));
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

                    case "degraded":
                        if (!classes.contains("degraded")) return false;
                        if (!iconSrc.contains("degraded")) return false;
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

            List<WebElement> indicators = safeFindAll(ioStatusColumn);

            for (WebElement indicator : indicators) {

                // TEXT (Healthy / Pending / Faulty)
                String text = safeGetText(indicator).trim().toLowerCase();

                // ICON
                String iconSrc = "";
                try {
                    WebElement img = safeFindWithin(indicator, By.tagName("img"));
                    iconSrc = img.getAttribute("src");
                } catch (Exception ignored) {}

                // INNER DIV CLASS
                String classes = "";
                try {
                    WebElement statusDiv =
                            safeFindWithin(indicator, By.cssSelector("div.module_status_indicator"));
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

    // -----------------------------
    // Last seen Verification
    // -----------------------------
    public boolean validateLastSeenValues() {

        System.out.println("\n=== VALIDATING LAST SEEN COLUMN ===");

        ensureListView();
        setRowsPerPageTo100();
        wait.waitForSeconds(1);

        int page = 1;

        while (true) {

            System.out.println("➡ Validating page " + page);
            waitForTableToLoad();

            List<WebElement> cells = safeFindAll(lastSeenCells);

            for (WebElement cell : cells) {
                String text = safeGetText(cell).trim().toLowerCase();

                System.out.println(" → Last seen: " + text);

                // Fail if empty
                if (text.isEmpty()) {
                    System.out.println(" EMPTY Last seen value detected!");
                    return false;
                }

                // Accepted formats: now, X minutes ago, X hours ago, X days ago, Yesterday,
                // also accept "a minute ago" and "an hour ago"
                boolean valid =
                        text.equals("now") ||
                                text.equals("yesterday") ||
                                text.matches("a minute ago") ||
                                text.matches("\\d+ minute[s]? ago") ||
                                text.matches("an hour ago") ||
                                text.matches("\\d+ hour[s]? ago") ||
                                text.matches("\\d+ day[s]? ago");


                if (!valid) {
                    System.out.println(" INVALID last seen format detected: " + text);
                    return false;
                }
            }

            // Move to next page
            if (!hasNextPage()) break;

            goToNextPage();
            page++;
        }

        System.out.println("✔ All last seen values validated successfully.");
        return true;
    }

    // -----------------------------------------------------------
    // Validate: Last Seen ⟶ Correct Network Status
    // -----------------------------------------------------------
    public boolean validateLastSeenVsNetworkStatus() {
        System.out.println("\n=== VALIDATING LAST SEEN ↔ NETWORK STATUS RELATION ===");

        ensureListView();
        setRowsPerPageTo100();
        waitForTableToLoad();

        int page = 1;

        while (true) {

            System.out.println("➡ Validating page " + page);

            List<WebElement> lastSeenCellsLocal = safeFindAll(By.cssSelector("td.cdk-column-last_seen"));

            List<WebElement> networkStatusCellsLocal = safeFindAll(By.cssSelector("td.cdk-column-network_status"));

            if (lastSeenCellsLocal.size() != networkStatusCellsLocal.size()) {
                System.out.println(" Column row count mismatch — cannot validate!");
                return false;
            }

            for (int i = 0; i < lastSeenCellsLocal.size(); i++) {

                String lastSeenText = safeGetText(lastSeenCellsLocal.get(i)).trim().toLowerCase();
                String networkStatusText = safeGetText(networkStatusCellsLocal.get(i)).trim().toLowerCase();

                System.out.println(" Row " + (i + 1) + ": Last Seen = " + lastSeenText +
                        " | Network Status = " + networkStatusText);

                boolean shouldBeOffline = false;
                boolean shouldBeOnline = false;

                if (lastSeenText.equals("now")) {
                    shouldBeOnline = true;
                } else if (lastSeenText.contains("hour") ||
                        lastSeenText.contains("day") ||
                        lastSeenText.contains("a minute ago") ||
                        lastSeenText.contains("minutes ago") ||
                        lastSeenText.contains("minute ago") ||
                        lastSeenText.contains("an hour ago")) {
                    shouldBeOffline = true;
                }

                // VALIDATE EXPECTATION
                if (shouldBeOnline && !networkStatusText.contains("online")) {
                    System.out.println(" MISMATCH: Last seen = " + lastSeenText
                            + " → EXPECTED ONLINE but got: " + networkStatusText);
                    return false;
                }

                if (shouldBeOffline && !networkStatusText.contains("offline")) {
                    System.out.println(" MISMATCH: Last seen = " + lastSeenText
                            + " → EXPECTED OFFLINE but got: " + networkStatusText);
                    return false;
                }
            }

            if (!hasNextPage()) break;

            goToNextPage();
            page++;
        }

        System.out.println("✔ All Last Seen ↔ Network Status relationships validated successfully.");
        return true;
    }

    private boolean isCommissioned(WebElement row) {
        WebElement nameSpan = safeFindWithin(row, moduleNameCell);
        String clazz = "";
        try {
            clazz = nameSpan.getAttribute("class").trim().toLowerCase();
        } catch (Exception ignored) {}
        return clazz.equals("na"); // commissioned modules
    }

    private String getNetworkStatus(WebElement row) {
        WebElement statusCell = safeFindWithin(row, By.cssSelector("td.cdk-column-network_status"));
        return safeGetText(statusCell).trim().toLowerCase();
    }

    private List<String> openActionMenuAndGetItems(WebElement button) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
            wait.waitForSeconds(1);

            safeClickElement(button);
            wait.waitForSeconds(1);

            // fetch overlay options fresh
            List<WebElement> options = safeFindAll(actionOverlayItems);
            List<String> texts = new ArrayList<>();

            for (WebElement opt : options) {
                texts.add(safeGetText(opt).trim());
            }

            return texts;

        } catch (Exception e) {
            System.out.println("ERROR reading action menu: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private void closeOverlay() {
        try {
            WebElement body = safeFind(By.tagName("body"));
            safeClickElement(body);
            wait.waitForSeconds(1);

            List<WebElement> backdrops = safeFindAll(By.cssSelector(".cdk-overlay-backdrop"));
            if (!backdrops.isEmpty()) {
                // try clicking offset to clear overlay
                new Actions(driver).moveByOffset(10, 10).click().perform();
                wait.waitForSeconds(1);
            }

        } catch (Exception ignored) {}
    }

    public boolean validateActionColumnLogic() {
        System.out.println("\n=== VALIDATING ACTION COLUMN LOGIC ===");

        ensureListView();
        setRowsPerPageTo100();
        waitForTableToLoad();

        int rowIndex = 1;

        while (true) {

            System.out.println("➡ Validating page rows...");

            // ALWAYS re-fetch rows fresh → prevents stale elements
            List<WebElement> rows = safeFindAll(By.cssSelector("app-module-manager-list-view table tbody tr"));

            for (int i = 0; i < rows.size(); i++) {

                // Re-fetch row each time to avoid stale element
                WebElement row = safeFindAll(By.cssSelector("app-module-manager-list-view table tbody tr")).get(i);

                boolean commissioned = isCommissioned(row);
                String network = getNetworkStatus(row);
                String normalized = network.toLowerCase();

                // Now safely fetch the button fresh
                WebElement button;
                try {
                    button = safeFindWithin(row, By.cssSelector("td.cdk-column-action button.khebab_action"));
                } catch (Exception e) {
                    System.out.println("Could not find action button in row " + rowIndex + " — skipping row.");
                    rowIndex++;
                    continue;
                }

                List<String> actualMenu = openActionMenuAndGetItems(button);

                // Retry once if empty (overlay hiccup)
                if (actualMenu.isEmpty()) {
                    wait.waitForSeconds(1);
                    try {
                        row = safeFindAll(By.cssSelector("app-module-manager-list-view table tbody tr")).get(i);
                        button = safeFindWithin(row, By.cssSelector("td.cdk-column-action button.khebab_action"));
                        actualMenu = openActionMenuAndGetItems(button);
                    } catch (Exception ignored) {}
                }

                closeOverlay();

                List<String> expected;

                if (commissioned) {
                    expected = Arrays.asList("Edit", "View Details", "Decommission", "Delete");
                } else if (normalized.contains("online")) {
                    expected = Arrays.asList("View Details", "Commission", "Delete");
                } else {
                    expected = Arrays.asList("View Details", "Delete");
                }

                if (!actualMenu.equals(expected)) {
                    System.out.println("\n ACTION MENU MISMATCH (Row " + rowIndex + ")");
                    System.out.println("Commissioned?: " + commissioned + " | Network: " + network);
                    System.out.println("Expected: " + expected);
                    System.out.println("Actual:   " + actualMenu);
                    return false;
                }

                System.out.println("✔ Row " + rowIndex + " OK → " + actualMenu);
                rowIndex++;
            }

            if (!hasNextPage()) break;

            goToNextPage();
            wait.waitForSeconds(1);
        }

        System.out.println("✔ ALL rows validated successfully.");
        return true;
    }




}



