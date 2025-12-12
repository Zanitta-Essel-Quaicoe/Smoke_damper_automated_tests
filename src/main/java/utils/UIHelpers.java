package utils;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;

public class UIHelpers {

    private WebDriver driver;
    private WaitUtils wait;
    private JavascriptExecutor js;
    private Actions actions;

    public UIHelpers(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.js = (JavascriptExecutor) driver;
        this.actions = new Actions(driver);
    }

    // -----------------------------
    // Basic interactions
    // -----------------------------

    public void click(By locator) {
        wait.waitForClickable(locator).click();
    }

    public void type(By locator, String text) {
        WebElement element = wait.waitForVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    public String getText(By locator) {
        return wait.waitForVisible(locator).getText();
    }

    public boolean isVisible(By locator) {
        try {
            return wait.waitForVisible(locator).isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    // -----------------------------
    // JavaScript interactions
    // -----------------------------

    public void clickUsingJS(By locator) {
        WebElement element = wait.waitForVisible(locator);
        js.executeScript("arguments[0].click();", element);
    }

    public void scrollIntoView(By locator) {
        WebElement element = wait.waitForVisible(locator);
        js.executeScript("arguments[0].scrollIntoView(true);", element);
    }

    public void highlight(By locator) {
        WebElement element = wait.waitForVisible(locator);
        js.executeScript("arguments[0].style.border='2px solid red'", element);
    }

    // -----------------------------
    // Advanced interactions
    // -----------------------------

    public void hover(By locator) {
        WebElement element = wait.waitForVisible(locator);
        actions.moveToElement(element).perform();
    }

    public void doubleClick(By locator) {
        WebElement element = wait.waitForClickable(locator);
        actions.doubleClick(element).perform();
    }

    public void rightClick(By locator) {
        WebElement element = wait.waitForClickable(locator);
        actions.contextClick(element).perform();
    }

    // -----------------------------
    // Safe actions (no crash)
    // -----------------------------

    public boolean safeClick(By locator) {
        try {
            click(locator);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean safeType(By locator, String text) {
        try {
            type(locator, text);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public WebElement retryFind(By locator) {
        for (int i = 0; i < 3; i++) {
            try {
                return driver.findElement(locator);
            } catch (StaleElementReferenceException e) {
                wait.waitForSeconds(1);
            }
        }
        throw new RuntimeException("Could not recover element: " + locator);
    }

    public void safeClick(WebElement element) {
        for (int i = 0; i < 3; i++) {
            try {
                element.click();
                return;
            } catch (StaleElementReferenceException e) {
                System.out.println("Retrying click due to stale element...");
                wait.waitForSeconds(1);
            }
        }
        throw new RuntimeException("Element still stale after retries!");
    }


}
