package pages;

import org.openqa.selenium.WebDriver;
import utils.WaitUtils;
import utils.UIHelpers;

public abstract class BasePage {

    protected WebDriver driver;
    protected WaitUtils wait;
    protected UIHelpers ui;

    // Constructor for all pages to use
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.ui = new UIHelpers(driver);
    }

    /**
     * Optional base navigation method
     * (Use if a page has a direct URL)
     */
    public void navigateTo(String url) {
        driver.get(url);
        wait.waitForPageLoad();
    }

    /**
     * Get page title
     */
    public String getPageTitle() {
        return driver.getTitle();
    }

    /**
     * Common method for verifying text, elements, etc. will be added later
     */
}
