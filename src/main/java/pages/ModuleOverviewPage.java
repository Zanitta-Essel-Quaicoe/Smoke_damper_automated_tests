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

    public ModuleOverviewPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.ui = new UIHelpers(driver);
        verifyPageLoaded();
        ensureListView();
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

    // 🟢 CORRECTED PAGINATION LOCATORS
    private By nextPageButton = By.cssSelector(
            ".paginator_navigation app-button:last-of-type button"
    );

    private By rangeText = By.cssSelector(".paginator_range_display");


    // -----------------------------
    // Verify Page Loaded
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
            System.out.println("❌ Module Overview FAILED to load.");
            throw e;
        }
    }


    // -----------------------------
    // Ensure LIST VIEW is active
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
        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(20));

        w.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(tableElement),
                ExpectedConditions.visibilityOfElementLocated(tableRows)
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


    // 🟢 CORRECTED: detect if next button is enabled
    private boolean hasNextPage() {
        try {
            WebElement next = driver.findElement(nextPageButton);
            return next.isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }


    // 🟢 CORRECTED: click next page reliably
    private void goToNextPage() {
        System.out.println("➡ Clicking NEXT PAGE...");
        ui.safeClick(nextPageButton);
        wait.waitForSeconds(2);
    }
}
