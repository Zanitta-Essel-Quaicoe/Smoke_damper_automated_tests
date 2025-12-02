package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import utils.UIHelpers;
import utils.WaitUtils;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SystemConfigurationPage {

    private WebDriver driver;
    private WaitUtils wait;
    private UIHelpers ui;

    // ==============================
    // Locators for main sections
    // ==============================
    private By pageHeader = By.xpath("//h1[contains(text(),'System Configuration')]");

    private By serialPortSection = By.xpath("//app-serial-port-config");
    private By tagConfigSection = By.xpath("//app-tag-configuration");
    private By terminalSection = By.xpath("//app-terminal");
    private By systemResetSection = By.xpath("//app-system-reset");

    // ==============================
    // Constructor
    // ==============================
    public SystemConfigurationPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.ui = new UIHelpers(driver);

        verifyPageLoaded();
    }

    // ==============================
    // Verify Page Loaded
    // ==============================
    private void verifyPageLoaded() {
        try {
            WebDriverWait strongWait = new WebDriverWait(driver, Duration.ofSeconds(20));

            strongWait.until(ExpectedConditions.or(
                    ExpectedConditions.visibilityOfElementLocated(pageHeader),
                    ExpectedConditions.visibilityOfElementLocated(serialPortSection),
                    ExpectedConditions.visibilityOfElementLocated(systemResetSection)
            ));

            System.out.println("✔ System Configuration page loaded successfully.");

        } catch (Exception e) {
            System.out.println(" FAILED: System Configuration page did NOT load properly.");
            throw e;
        }
    }

    // ==============================
    // Section Visibility Checkers
    // ==============================
    public boolean isSerialPortSectionVisible() {
        return driver.findElement(serialPortSection).isDisplayed();
    }

    public boolean isTagConfigSectionVisible() {
        return driver.findElement(tagConfigSection).isDisplayed();
    }

    public boolean isTerminalSectionVisible() {
        return driver.findElement(terminalSection).isDisplayed();
    }

    public boolean isSystemResetSectionVisible() {
        return driver.findElement(systemResetSection).isDisplayed();
    }
}
