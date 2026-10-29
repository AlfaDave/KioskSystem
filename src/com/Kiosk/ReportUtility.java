/**
 * Generates kiosk usage reports, including current stats, historical snapshots, and CSV exports.
 * Reads .data files, computes demographic metrics, and saves .datal snapshots.
 * Provides a console report and a downloadable CSV for external analysis.
 */

package com.Kiosk;

import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Scanner;

public class ReportUtility {

    public void generateReport(String companyInfoPath) {
        clearConsole();
        System.out.println("====================================");
        System.out.println("         KIOSK USAGE REPORT");
        System.out.println("====================================");

        try {
            if (companyInfoPath == null) {
                System.out.println("No company info file found. Cannot generate report.");
                return;
            }

            File companyInfoFile = new File(companyInfoPath);
            File companyDir = companyInfoFile.getParentFile();
            File safeRoot = companyDir.getParentFile();
            String baseFolder = safeRoot.getAbsolutePath() + "\\NewUserSignUps\\";

            File signUpsFile = new File(baseFolder + "SignUps.data");
            File advertiseFile = new File(baseFolder + "advertise.data");
            File proFile = new File(baseFolder + "prosignups.data");

            ReportStats current = readCurrentStats(signUpsFile, advertiseFile, proFile);
            ReportSnapshot previous = loadLatestSnapshot(baseFolder);

            System.out.println("Company Folder: " + safeRoot.getAbsolutePath());
            System.out.println("Total Signups: " + current.totalSignups);
            System.out.println("Pro Signups: " + current.proSignups);
            System.out.println("Advertise Signups: " + current.advertiseSignups);
            System.out.printf("Average Age: %.2f\n", current.averageAge);
            System.out.println("Male Count: " + current.maleCount);
            System.out.println("Female Count: " + current.femaleCount);
            System.out.println("Other Count: " + current.otherCount);
            System.out.println("------------------------------------");

            if (previous != null) {
                System.out.println("Comparison with last report:");
                System.out.println("Previous Total Signups: " + previous.totalSignups +
                        " (Change: " + (current.totalSignups - previous.totalSignups) + ")");
                System.out.println("Previous Pro Signups: " + previous.proSignups +
                        " (Change: " + (current.proSignups - previous.proSignups) + ")");
                System.out.println("Previous Advertise Signups: " + previous.advertiseSignups +
                        " (Change: " + (current.advertiseSignups - previous.advertiseSignups) + ")");
                System.out.printf("Previous Average Age: %.2f (Change: %.2f)\n",
                        previous.averageAge, current.averageAge - previous.averageAge);
                System.out.println("Previous Male Count: " + previous.maleCount +
                        " (Change: " + (current.maleCount - previous.maleCount) + ")");
                System.out.println("Previous Female Count: " + previous.femaleCount +
                        " (Change: " + (current.femaleCount - previous.femaleCount) + ")");
                System.out.println("Previous Other Count: " + previous.otherCount +
                        " (Change: " + (current.otherCount - previous.otherCount) + ")");
            } else {
                System.out.println("This is the first recorded report.");
                System.out.println("No previous demographic data available.");
            }

            System.out.println("------------------------------------");

            String timestamp = java.time.LocalDateTime.now().toString().replace(":", "-");
            saveSnapshot(baseFolder, current, timestamp);
            String csvPath = writeCsv(baseFolder, current, previous, timestamp);

            System.out.println("A CSV report has also been created:");
            System.out.println(csvPath);
            System.out.println("------------------------------------");
            System.out.println("Press ENTER to return to kiosk...");
            new Scanner(System.in).nextLine();

        } catch (Exception e) {
            System.out.println("Error generating report.");
            e.printStackTrace();
        }
    }

    private ReportStats readCurrentStats(File signUpsFile, File advertiseFile, File proFile) {
        ReportStats stats = new ReportStats();

        stats.proSignups = countLines(proFile);
        stats.advertiseSignups = countLines(advertiseFile);

        if (!signUpsFile.exists()) return stats;

        int total = 0;
        int ageSum = 0;
        int ageCount = 0;
        int male = 0, female = 0, other = 0;

        try (Scanner scanner = new Scanner(signUpsFile)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                if (line.startsWith("Age:")) {
                    String value = line.substring(4).trim();
                    try {
                        int age = Integer.parseInt(value);
                        ageSum += age;
                        ageCount++;
                    } catch (Exception ignored) {}
                }

                else if (line.startsWith("Sex:")) {
                    String sex = line.substring(4).trim();
                    if (sex.equalsIgnoreCase("Male")) male++;
                    else if (sex.equalsIgnoreCase("Female")) female++;
                    else other++;
                }

                else if (line.startsWith("====================================")) {
                    total++;
                }
            }
        } catch (Exception ignored) {}

        stats.totalSignups = total;
        stats.maleCount = male;
        stats.femaleCount = female;
        stats.otherCount = other;
        stats.averageAge = (ageCount == 0) ? 0.0 : (ageSum * 1.0 / ageCount);

        return stats;
    }

    private int countLines(File file) {
        if (file == null || !file.exists()) return 0;
        int count = 0;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                scanner.nextLine();
                count++;
            }
        } catch (Exception ignored) {}
        return count;
    }

    private ReportSnapshot loadLatestSnapshot(String baseFolder) {
        File folder = new File(baseFolder);
        if (!folder.exists()) return null;

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".datal"));
        if (files == null || files.length == 0) return null;

        Arrays.sort(files, Comparator.comparing(File::getName));
        return parseSnapshot(files[files.length - 1]);
    }

    private ReportSnapshot parseSnapshot(File file) {
        ReportSnapshot snap = new ReportSnapshot();

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                if (line.startsWith("TotalSignups="))
                    snap.totalSignups = Integer.parseInt(line.substring(13).trim());

                else if (line.startsWith("ProSignups="))
                    snap.proSignups = Integer.parseInt(line.substring(11).trim());

                else if (line.startsWith("AdvertiseSignups="))
                    snap.advertiseSignups = Integer.parseInt(line.substring(17).trim());

                else if (line.startsWith("AverageAge="))
                    snap.averageAge = Double.parseDouble(line.substring(11).trim());

                else if (line.startsWith("MaleCount="))
                    snap.maleCount = Integer.parseInt(line.substring(10).trim());

                else if (line.startsWith("FemaleCount="))
                    snap.femaleCount = Integer.parseInt(line.substring(12).trim());

                else if (line.startsWith("OtherCount="))
                    snap.otherCount = Integer.parseInt(line.substring(11).trim());
            }
        } catch (Exception ignored) {}

        return snap;
    }

    private void saveSnapshot(String baseFolder, ReportStats stats, String timestamp) {
        ensureFolder(baseFolder);
        String path = baseFolder + "Report_" + timestamp + ".datal";

        try (FileWriter writer = new FileWriter(path)) {
            writer.write("TotalSignups=" + stats.totalSignups + "\n");
            writer.write("ProSignups=" + stats.proSignups + "\n");
            writer.write("AdvertiseSignups=" + stats.advertiseSignups + "\n");
            writer.write("AverageAge=" + stats.averageAge + "\n");
            writer.write("MaleCount=" + stats.maleCount + "\n");
            writer.write("FemaleCount=" + stats.femaleCount + "\n");
            writer.write("OtherCount=" + stats.otherCount + "\n");
        } catch (Exception ignored) {}
    }

    private String writeCsv(String baseFolder, ReportStats current, ReportSnapshot previous, String timestamp) {
        ensureFolder(baseFolder);
        String csvPath = baseFolder + "Report_" + timestamp + ".csv";

        try (FileWriter csv = new FileWriter(csvPath)) {

            // ============================
            // CURRENT REPORT SECTION
            // ============================
            csv.write("=== CURRENT REPORT ===\n");
            csv.write("Metric,Value\n");
            csv.write("Total Signups," + current.totalSignups + "\n");
            csv.write("Pro Signups," + current.proSignups + "\n");
            csv.write("Advertise Signups," + current.advertiseSignups + "\n");
            csv.write("Average Age," + current.averageAge + "\n");
            csv.write("Male Count," + current.maleCount + "\n");
            csv.write("Female Count," + current.femaleCount + "\n");
            csv.write("Other Count," + current.otherCount + "\n");
            csv.write("\n");

            if (previous != null) {

                // ============================
                // PREVIOUS REPORT SECTION
                // ============================
                csv.write("=== PREVIOUS REPORT ===\n");
                csv.write("Metric,Value\n");
                csv.write("Previous Total Signups," + previous.totalSignups + "\n");
                csv.write("Previous Pro Signups," + previous.proSignups + "\n");
                csv.write("Previous Advertise Signups," + previous.advertiseSignups + "\n");
                csv.write("Previous Average Age," + previous.averageAge + "\n");
                csv.write("Previous Male Count," + previous.maleCount + "\n");
                csv.write("Previous Female Count," + previous.femaleCount + "\n");
                csv.write("Previous Other Count," + previous.otherCount + "\n");
                csv.write("\n");

                // ============================
                // CHANGE REPORT SECTION
                // ============================
                csv.write("=== CHANGE REPORT ===\n");
                csv.write("Metric,Value\n");
                csv.write("Change Total Signups," + (current.totalSignups - previous.totalSignups) + "\n");
                csv.write("Change Pro Signups," + (current.proSignups - previous.proSignups) + "\n");
                csv.write("Change Advertise Signups," + (current.advertiseSignups - previous.advertiseSignups) + "\n");
                csv.write("Change Average Age," + (current.averageAge - previous.averageAge) + "\n");
                csv.write("Change Male Count," + (current.maleCount - previous.maleCount) + "\n");
                csv.write("Change Female Count," + (current.femaleCount - previous.femaleCount) + "\n");
                csv.write("Change Other Count," + (current.otherCount - previous.otherCount) + "\n");

            } else {

                // ============================
                // FIRST REPORT CASE
                // ============================
                csv.write("=== PREVIOUS REPORT ===\n");
                csv.write("Previous Data,First report - no previous snapshot\n");
            }

        } catch (Exception ignored) {}

        return csvPath;
    }


    private void clearConsole() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("win")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) System.out.println();
        }
    }

    private static class ReportStats {
        int totalSignups;
        int proSignups;
        int advertiseSignups;
        double averageAge;
        int maleCount;
        int femaleCount;
        int otherCount;
    }

    private static class ReportSnapshot {
        int totalSignups;
        int proSignups;
        int advertiseSignups;
        double averageAge;
        int maleCount;
        int femaleCount;
        int otherCount;
    }
    private void ensureFolder(String folderPath) {
        File f = new File(folderPath);
        if (!f.exists()) {
            f.mkdirs();
        }
    }

}
