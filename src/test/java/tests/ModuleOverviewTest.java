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
    public void test_Filter_By_Faulty_Modules() {
        System.out.println("\n=== TEST: Filter by Faulty ===");

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage moduleOverview = sidebar.goToModuleOverview();

        ModuleOverviewPage page = new ModuleOverviewPage(driver);

        // Apply filter
        page.applyStatusFilter("Faulty");

        // Validate correctness across all pages
        boolean result = page.validateStatusAcrossAllPages("Faulty");

        Assertions.assertTrue(result, " Some modules did not match Faulty filter!");
        System.out.println("✔ Filter by Faulty validated successfully.");
    }


    @Test
    public void test_Filter_By_Healthy_Modules() {
        System.out.println("\n=== TEST: Filter by Healthy ===");

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage moduleOverview = sidebar.goToModuleOverview();

        ModuleOverviewPage page = new ModuleOverviewPage(driver);

        // Apply filter
        page.applyStatusFilter("Healthy");

        // Validate correctness across all pages
        boolean result = page.validateStatusAcrossAllPages("Healthy");

        Assertions.assertTrue(result, " Some modules did not match Healthy filter!");
        System.out.println("✔ Filter by Healthy validated successfully.");
    }

    @Test
    public void testResetFilterShowsAllModules() {

        SidebarPage sidebar = new SidebarPage(driver);
        ModuleOverviewPage modulePage = sidebar.goToModuleOverview();

        // Apply a filter first
        modulePage.applyStatusFilter("Healthy");
        boolean filtered = modulePage.validateStatusAcrossAllPages("Healthy");

        Assertions.assertTrue(filtered, " Filter did not apply correctly before reset.");

        // Now reset
        modulePage.resetFilters();

        // Validate reset
        boolean resetOk = modulePage.validateResetShowsAllModules();

        Assertions.assertTrue(resetOk, " Reset filter did not restore full module list!");

        System.out.println("✔ Reset filter test passed.");
    }


}
