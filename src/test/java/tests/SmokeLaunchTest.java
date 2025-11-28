package tests;

import base.BaseTest;
import org.junit.jupiter.api.Test;

public class SmokeLaunchTest extends BaseTest {

    @Test
    public void launchPageTest() {
        driver.get(baseUrl);
        demoPause(1000); //to slow automation

        // Wait for page load
        wait.waitForPageLoad();

        System.out.println("Page title is: " + driver.getTitle());
    }
}
