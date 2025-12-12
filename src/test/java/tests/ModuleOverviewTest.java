package tests;

import base.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.DashboardPage;
import pages.ModuleOverviewPage;
import pages.SidebarPage;

public class ModuleOverviewTest extends BaseTest {

    @Test
    @DisplayName("Filter Modules By Network Status - Connected")
    public void testFilterByConnected() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage page = sidebar.goToModuleOverview();

        page.applyNetworkFilter("Connected");

        boolean result = page.validateNetworkFilterResults("Connected");

        Assertions.assertTrue(result, " Network Status filter 'Connected' failed!");

        System.out.println("✔ Network Status 'Connected' filter validated successfully.");
    }

    @Test
    @DisplayName("Filter Modules By Network Status - Disconnected")
    public void testFilterByDisconnected() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage page = sidebar.goToModuleOverview();

        page.applyNetworkFilter("Disconnected");

        boolean result = page.validateNetworkFilterResults("Disconnected");

        Assertions.assertTrue(result, " Network Status filter 'Disconnected' failed!");

        System.out.println("✔ Network Status 'Disconnected' filter validated successfully.");
    }

    @Test
    @DisplayName("Filter Modules By Network Status - Pending")
    public void testFilterByPendingd() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage page = sidebar.goToModuleOverview();

        page.applyNetworkFilter("Pending");

        boolean result = page.validateNetworkFilterResults("Pending");

        Assertions.assertTrue(result, " Network Status filter 'Disconnected' failed!");

        System.out.println("✔ Network Status 'Pending' filter validated successfully.");
    }

    @Test
    @DisplayName("Filter Modules By Network Status - Degraded")
    public void testFilterByDegraded() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage page = sidebar.goToModuleOverview();

        page.applyNetworkFilter("Degraded");

        boolean result = page.validateNetworkFilterResults("Degraded");

        Assertions.assertTrue(result, " Network Status filter 'Degraded' failed!");

        System.out.println("✔ Network Status 'Degraded' filter validated successfully.");
    }

    @Test
    @DisplayName("Filter Modules By I/O Status - Healthy")
    public void testFilterIOHealthy() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage page = sidebar.goToModuleOverview();

        page.applyIOFilter("Healthy");

        boolean result = page.validateIOFilterResults("Healthy");

        Assertions.assertTrue(result, " I/O Filter 'Healthy' failed!");
    }

    @Test
    @DisplayName("Filter Modules By I/O Status - Faulty")
    public void testFilterIOFaulty() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage page = sidebar.goToModuleOverview();

        page.applyIOFilter("Faulty");

        boolean result = page.validateIOFilterResults("Faulty");

        Assertions.assertTrue(result, " I/O Filter 'Faulty' failed!");
    }

    @Test
    @DisplayName("Filter Modules By I/O Status - Pending")
    public void testFilterIOPending() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage page = sidebar.goToModuleOverview();

        page.applyIOFilter("Pending");

        boolean result = page.validateIOFilterResults("Pending");

        Assertions.assertTrue(result, " I/O Filter 'Pending' failed!");
    }

    @Test
    @DisplayName("Filter by Commissioned")
    public void testFilterCommissioned() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage page = sidebar.goToModuleOverview();

        page.applyAllFilter("Commissioned");
        boolean result = page.validateAllFilterResults("Commissioned");

        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("Filter by Uncommissioned")
    public void testFilterUncommissioned() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage page = sidebar.goToModuleOverview();

        page.applyAllFilter("Uncommissioned");
        boolean result = page.validateAllFilterResults("Uncommissioned");

        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("Filter by All")
    public void testFilterAll() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage page = sidebar.goToModuleOverview();

        page.applyAllFilter("All");
        boolean result = page.validateAllFilterResults("All");

        Assertions.assertTrue(result);
    }

    @Test
    @DisplayName("Verify Reset Filter clears all applied filters")
    public void testResetFilters() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage page = sidebar.goToModuleOverview();

        // Apply filters
        page.applyNetworkFilter("Disconnected");
        page.applyIOFilter("Faulty");
        page.applyAllFilter("Uncommissioned");

        // Reset
        boolean resetOK = page.resetFiltersNew();

        Assertions.assertTrue(resetOK, " Reset did not clear filters properly!");
    }


    @Test
    @DisplayName("Validate Module Name column is not empty and has no duplicates")
    public void testModuleNameValidity() {

        System.out.println("\n=== TEST: Validate Module Name Column ===");

        DashboardPage dashboard = new DashboardPage(driver);
        SidebarPage sidebar = new SidebarPage(driver);

        ModuleOverviewPage moduleOverview = sidebar.goToModuleOverview();

        boolean result = moduleOverview.validateModuleNames();

        Assertions.assertTrue(result, " Module Name validation failed!");
    }


    @Test
    @DisplayName("Validate that all Module IPs are correct, non-unknown, and unique")
    public void testModuleIPValidity() {

        System.out.println("\n=== TEST: Validate Module IP Assignments ===");

        new DashboardPage(driver);

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage moduleOverview = sidebar.goToModuleOverview();

        boolean result = moduleOverview.validateModuleIPs();

        Assertions.assertTrue(result, " Module IP validation failed!");
    }

    @Test
    @DisplayName("Validate Network Status column (Online / Degraded / Offline / Pending)")
    public void testNetworkStatusValidity() {

        System.out.println("\n=== TEST: Validate Network Status Column ===");

        new DashboardPage(driver);
        SidebarPage sidebar = new SidebarPage(driver);

        ModuleOverviewPage moduleOverview = sidebar.goToModuleOverview();

        boolean result = moduleOverview.validateNetworkStatus();

        Assertions.assertTrue(result, " Network Status validation failed!");
    }

    @Test
    @DisplayName("Validate I/O Status column (Healthy, Pending, Faulty)")
    public void testIOStatusValidity() {

        System.out.println("\n=== TEST: Validate I/O Status Column ===");

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage moduleOverview = sidebar.goToModuleOverview();

        boolean result = moduleOverview.validateIOStatusAcrossPages();

        Assertions.assertTrue(result, " I/O Status validation failed!");
    }

    @Test
    @DisplayName("Validate Last Seen Column Formatting and Values")
    public void testLastSeenColumnValidity() {

        System.out.println("\n=== TEST: Validate Last Seen Column ===");

        new DashboardPage(driver);
        SidebarPage sidebar = new SidebarPage(driver);

        ModuleOverviewPage modulePage = sidebar.goToModuleOverview();

        boolean result = modulePage.validateLastSeenValues();

        Assertions.assertTrue(result, " Last Seen column validation failed!");
    }

    @Test
    @DisplayName("Validate Last Seen correlates correctly with Network Status")
    public void testLastSeenVsNetworkStatus() {

        System.out.println("\n=== TEST: Validate Last Seen ↔ Network Status ===");

        new DashboardPage(driver);
        SidebarPage sidebar = new SidebarPage(driver);

        ModuleOverviewPage modulePage = sidebar.goToModuleOverview();

        boolean result = modulePage.validateLastSeenVsNetworkStatus();

        Assertions.assertTrue(result, " Last Seen ↔ Network Status validation failed!");
    }

    @Test
    @DisplayName("Validate Action Dropdown Logic Based on Module State")
    public void testActionColumnLogic() {

        System.out.println("\n=== TEST: Validate Action Column Logic ===");

        new DashboardPage(driver);
        SidebarPage sidebar = new SidebarPage(driver);

        ModuleOverviewPage modulePage = sidebar.goToModuleOverview();

        boolean result = modulePage.validateActionColumnLogic();

        Assertions.assertTrue(result, "} Action column logic validation failed!");
    }



}
