package utils;

import java.io.File;
import java.util.Arrays;

public class DownloadUtils {

    private static final String DOWNLOAD_DIR = "C:\\SeleniumDownloads"; // keep same as DriverFactory
    private static final int MAX_ATTEMPTS = 45; // seconds
    private static final int STABLE_CHECKS = 2; // require size stable for this many checks

    // Deletes all existing files before export test
    public static void clearDownloads() {
        File dir = new File(DOWNLOAD_DIR);
        if (!dir.exists()) return;

        File[] files = dir.listFiles();
        if (files == null) return;
        Arrays.stream(files).forEach(File::delete);
    }

    // Wait until a .xlsx file with name starting with "Modules" appears and is fully written
    public static File waitForDownloadedFile() throws InterruptedException {
        File dir = new File(DOWNLOAD_DIR);
        if (!dir.exists()) return null;

        int attempts = 0;
        while (attempts < MAX_ATTEMPTS) {
            Thread.sleep(1000); // 1 second

            File[] files = dir.listFiles((d, name) ->
                    name.startsWith("Modules") && name.toLowerCase().endsWith(".xlsx")
            );

            if (files != null && files.length > 0) {
                // pick the newest file
                File candidate = Arrays.stream(files)
                        .sorted((a, b) -> Long.compare(b.lastModified(), a.lastModified()))
                        .findFirst()
                        .orElse(files[0]);

                // ensure file is fully written by checking size stability
                long previousSize = -1;
                boolean stable = true;
                for (int i = 0; i < STABLE_CHECKS; i++) {
                    long size = candidate.length();
                    if (previousSize != -1 && size != previousSize) {
                        stable = false;
                        break;
                    }
                    previousSize = size;
                    Thread.sleep(1000);
                }

                if (stable && candidate.exists() && candidate.length() > 0) {
                    return candidate;
                }
                // else loop again (file still being written)
            }

            attempts++;
        }

        return null; // timed out
    }
}
