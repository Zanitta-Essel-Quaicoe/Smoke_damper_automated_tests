package base;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import utils.PropertyReader;
import utils.WaitUtils;

public abstract class BaseTest {

    protected WebDriver driver;
    protected String baseUrl;
    protected WaitUtils wait;

    @BeforeEach
    public void setUp() {
        baseUrl = PropertyReader.get("baseUrl");

        DriverFactory.initializeDriver();
        driver = DriverFactory.getDriver();

        wait = new WaitUtils(driver);
        driver.get(baseUrl);
    }

    //Demo only to slow down automation
    protected static final boolean DEMO_MODE = true;

    protected void demoPause(long millis) {
        if (DEMO_MODE) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }



    @AfterEach
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}
