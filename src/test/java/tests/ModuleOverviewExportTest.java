package tests;

import base.BaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pages.DashboardPage;
import pages.ModuleOverviewPage;
import utils.DownloadUtils;

import java.io.File;

public class ModuleOverviewExportTest extends BaseTest {

    @Test
    @DisplayName("Verify Export functionality on Module Overview page")
    public void testModuleOverviewExport() throws InterruptedException {

        System.out.println("\n=== TEST: Module Overview Export ===");

        DashboardPage dashboard = new DashboardPage(driver);
        dashboard.clickViewDeviceList();

        // Create ModuleOverviewPage WITHOUT forcing List View
        ModuleOverviewPage moduleOverview = new ModuleOverviewPage(driver, true);

        // Clear old downloaded files
        System.out.println("➡ Clearing previous downloads...");
        DownloadUtils.clearDownloads();

        // Click Export
        moduleOverview.clickExport();

        // Wait for downloaded file
        File downloadedFile = DownloadUtils.waitForDownloadedFile();

        // Validate file exists
        Assertions.assertNotNull(downloadedFile, " No exported file was downloaded!");
        Assertions.assertTrue(downloadedFile.exists(), " Exported file does not exist!");

        System.out.println("✔ Export successful. Downloaded file: " + downloadedFile.getName());
    }
}
