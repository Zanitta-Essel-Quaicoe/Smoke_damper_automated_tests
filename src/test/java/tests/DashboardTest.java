package tests;

import base.BaseTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.DashboardPage;
import pages.ModuleOverviewPage;

public class DashboardTest extends BaseTest {

    @Test
    @DisplayName("Verify Dashboard loads successfully")
    public void testDashboardLoad() {

        DashboardPage dashboard = new DashboardPage(driver);

        System.out.println("\n=== Test: Dashboard Load Verification ===");

        dashboard.isDashboardHeaderVisible();

        System.out.println("✔ Test completed: Dashboard loaded and verified.");
    }

    @Test
    @DisplayName("Verify Dashboard Total Modules equals Module Overview count")
    public void testDashboardTotalModulesMatchesList() {

        System.out.println("\n=== TEST: Dashboard Total Modules vs Module Overview ===");

        DashboardPage dashboard = new DashboardPage(driver);

        int dashboardTotal = dashboard.getTotalModulesCount();

        ModuleOverviewPage moduleOverview = dashboard.clickViewDeviceList();

        int listTotal = moduleOverview.countAllModules();

        System.out.println(" Dashboard Total = " + dashboardTotal);
        System.out.println(" Module Overview Total = " + listTotal);

        org.junit.jupiter.api.Assertions.assertEquals(
                dashboardTotal,
                listTotal,
                " Mismatch: Dashboard total and Module Overview total differ!"
        );

        System.out.println("✔ Test Passed: Dashboard Total matches Module Overview count.");
    }

    @Test
    @DisplayName("Verify navigation to Module Overview page via 'View all' button")
    public void testNavigateToModuleOverviewViaViewAll() {

        DashboardPage dashboard = new DashboardPage(driver);

        System.out.println("\n=== Test: Navigate to Module Overview (Grid View) ===");

        dashboard.clickViewAllModules();

        System.out.println("✔ Navigated to Module Overview page from Dashboard.");
    }

    @Test
    @DisplayName("Verify navigation to Module Overview via 'View device list'")
    public void testNavigateToModuleOverviewListView() {

        DashboardPage dashboard = new DashboardPage(driver);

        System.out.println("\n=== Test: Navigate to Module Overview via 'View device list' ===");

        dashboard.clickViewDeviceList();

        System.out.println("✔ Navigated to Module Overview page via 'View device list'.");
    }

    @Test
    @DisplayName("Verify navigation to Event Logs via Dashboard 'View all'")
    public void testNavigateToEventLogs() {

        DashboardPage dashboard = new DashboardPage(driver);

        System.out.println("\n=== Test: Navigate to Event Logs page ===");

        dashboard.clickViewAllEventLogs();

        System.out.println("✔ Navigated to Event Logs page.");
    }
}
