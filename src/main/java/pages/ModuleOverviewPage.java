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

    // =========================
    // Network Status Filter
    // =========================
    public void applyNetworkFilter(String value) {
        System.out.println("➡ Applying Network Status Filter: " + value);

        // Open dropdown
        ui.safeClick(networkFilterButton);
        wait.waitForSeconds(1);

        // Wait for overlay options
        List<WebElement> options = wait.waitForVisibleElements(networkFilterOptions);

        boolean found = false;

        for (WebElement opt : options) {
            if (opt.getText().trim().equalsIgnoreCase(value)) {
                opt.click();
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

            List<WebElement> statuses =
                    driver.findElements(By.cssSelector("td.mat-column-network_status"));

            if (statuses.isEmpty()) {
                System.out.println("⚠ No rows found on page " + page);
            }

            int pageMatched = 0;

            for (WebElement cell : statuses) {
                String text = cell.getText().trim().toLowerCase();

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

        ui.safeClick(ioFilterButton);
        wait.waitForSeconds(1);

        List<WebElement> options = wait.waitForVisibleElements(ioFilterOptions);

        boolean found = false;

        for (WebElement opt : options) {
            if (opt.getText().trim().equalsIgnoreCase(value)) {
                opt.click();
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

            List<WebElement> statuses = driver.findElements(ioStatusColumn);

            for (WebElement cell : statuses) {
                totalRowsChecked++;

                String text = cell.getText().trim().toLowerCase();

                if (text.contains(expected)) {
                    matchedCount++;
                } else {
                    System.out.println(
                            " MISMATCH → Expected: " + expected + " | Found: " + text
                    );
                    return false;
                }
            }

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

            List<WebElement> names = driver.findElements(moduleNameCells);

            for (WebElement cell : names) {
                String name = cell.getText().trim();

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

            List<WebElement> cells = driver.findElements(lastSeenCells);

            for (WebElement cell : cells) {
                String text = cell.getText().trim().toLowerCase();

                System.out.println(" → Last seen: " + text);

                // Fail if empty
                if (text.isEmpty()) {
                    System.out.println(" EMPTY Last seen value detected!");
                    return false;
                }

                // Accepted formats: now, X minutes ago, X hours ago, X days ago, Yesterday
                boolean valid =
                        text.equals("now") ||
                                text.equals("yesterday") ||
                                text.matches("\\d+ minute[s]? ago") ||
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

            List<WebElement> lastSeenCells = driver.findElements(
                    By.cssSelector("td.cdk-column-last_seen")
            );

            List<WebElement> networkStatusCells = driver.findElements(
                    By.cssSelector("td.cdk-column-network_status")
            );

            if (lastSeenCells.size() != networkStatusCells.size()) {
                System.out.println(" Column row count mismatch — cannot validate!");
                return false;
            }

            for (int i = 0; i < lastSeenCells.size(); i++) {

                String lastSeenText = lastSeenCells.get(i).getText().trim().toLowerCase();
                String networkStatusText = networkStatusCells.get(i).getText().trim().toLowerCase();

                System.out.println(" Row " + (i + 1) + ": Last Seen = " + lastSeenText +
                        " | Network Status = " + networkStatusText);

                // ---------------------------
                // RULES:
                // "now" → Online
                // "a minute ago" → Offline
                // "1 minute ago" → Offline
                // "X minutes ago" (X ≥ 1) → Offline
                // hours / days → Offline
                // ---------------------------

                boolean shouldBeOffline = false;
                boolean shouldBeOnline = false;

                if (lastSeenText.equals("now")) {
                    shouldBeOnline = true;
                }
                else if (lastSeenText.contains("hour") ||
                        lastSeenText.contains("day") ||
                        lastSeenText.contains("a minute ago") ||
                        lastSeenText.contains("minutes ago") ||
                        lastSeenText.contains("minute ago")) {
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
        WebElement nameSpan = row.findElement(moduleNameCell);
        String clazz = nameSpan.getAttribute("class").trim().toLowerCase();
        return clazz.equals("na"); // commissioned modules
    }

    private String getNetworkStatus(WebElement row) {
        WebElement statusCell = row.findElement(By.cssSelector("td.cdk-column-network_status"));
        return statusCell.getText().trim().toLowerCase();
    }

    private List<String> openActionMenuAndGetItems(WebElement button) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
            wait.waitForSeconds(1);

            button.click();
            wait.waitForSeconds(1);

            List<WebElement> options = driver.findElements(actionOverlayItems);
            List<String> texts = new ArrayList<>();

            for (WebElement opt : options) {
                texts.add(opt.getText().trim());
            }

            return texts;

        } catch (Exception e) {
            System.out.println("ERROR reading action menu: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private void closeOverlay() {
        try {
            WebElement body = driver.findElement(By.tagName("body"));
            body.click();
            wait.waitForSeconds(1);

            if (!driver.findElements(By.cssSelector(".cdk-overlay-backdrop")).isEmpty()) {
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

        int page = 1;

        while (true) {
            System.out.println("➡ Validating page " + page);

            List<WebElement> rows = driver.findElements(
                    By.cssSelector("app-module-manager-list-view table tbody tr")
            );

            int rowIndex = 1;

            for (WebElement row : rows) {

                boolean commissioned = isCommissioned(row);
                String network = getNetworkStatus(row).toLowerCase();

                WebElement button = row.findElement(actionButtons);

                // Open overlay
                List<String> actualMenu = openActionMenuAndGetItems(button);

                // Close overlay
                closeOverlay();

                // Define expected items
                List<String> expected;

                if (commissioned) {
                    expected = Arrays.asList("Edit", "View Details", "Decommission", "Delete");
                } else if (network.contains("online")) {
                    expected = Arrays.asList("View Details", "Commission", "Delete");
                } else {
                    expected = Arrays.asList("View Details", "Delete");
                }

                // Compare
                if (!actualMenu.equals(expected)) {
                    System.out.println("\n ACTION MENU MISMATCH (Page " + page + ", Row " + rowIndex + ")");
                    System.out.println("Commissioned?: " + commissioned);
                    System.out.println("Network: " + network);
                    System.out.println("Expected: " + expected);
                    System.out.println("Actual:   " + actualMenu);
                    return false;
                }

                System.out.println("✔ Page " + page + " Row " + rowIndex + " OK → " + actualMenu);
                rowIndex++;
            }

            // Pagination check
            if (!hasNextPage()) break;

            goToNextPage();
            waitForTableToLoad();
            page++;
        }

        System.out.println("✔ All Action menu validations passed across all pages.");
        return true;
    }




}
