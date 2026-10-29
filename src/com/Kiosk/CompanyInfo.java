/**
 * Handles loading, saving, and resetting company configuration data.
 * Manages the company.info file and provides access to timeout and reset settings.
 * Integrates with the report system through deployReportUtility().
 */

package com.Kiosk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class CompanyInfo {

    private final String path;

    CompanyInfo() {
        String docs = System.getProperty("user.home") + "\\Documents\\";
        this.path = findCompanyInfo(new File(docs));
    }

    CompanyInfo(String companyDisplayName) {
        String safe = sanitizeForFolder(companyDisplayName);
        String companyRoot = System.getProperty("user.home")
                + "\\Documents\\" + safe + "\\Company\\";
        this.path = companyRoot + "company.info";
    }

    private String findCompanyInfo(File root) {
        if (root == null || !root.exists() || !root.isDirectory()) return null;
        File[] files = root.listFiles();
        if (files == null) return null;

        for (File f : files) {
            if (f.isDirectory()) {
                if (f.getName().equalsIgnoreCase("Company")) {
                    File info = new File(f, "company.info");
                    if (info.exists()) return info.getAbsolutePath();
                }
                String found = findCompanyInfo(f);
                if (found != null) return found;
            }
        }
        return null;
    }

    boolean exists() {
        return path != null && new File(path).exists();
    }

    void save(String companyDisplayName, int timeoutSeconds, String resetCode) {
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write("company=" + companyDisplayName + "\n");
            writer.write("timeout=" + timeoutSeconds + "\n");
            writer.write("resetcode=" + resetCode + "\n");
            writer.close();
        } catch (IOException error) {
            error.printStackTrace();
        }
    }
    void deployReportUtility() {
        ReportUtility util = new ReportUtility();
        util.generateReport(path);
    }

    String loadCompany() {
        try (Scanner scanner = new Scanner(new File(path))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("company=")) return line.substring(8);
            }
        } catch (Exception ignored) {}
        return "Unknown";
    }

    int loadTimeoutSeconds() {
        try (Scanner scanner = new Scanner(new File(path))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("timeout=")) {
                    String value = line.substring(8).trim();
                    try {
                        int t = Integer.parseInt(value);
                        if (t >= 30 && t <= 600) return t;
                    } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception ignored) {}
        return 120;
    }

    String loadResetCode() {
        try (Scanner scanner = new Scanner(new File(path))) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("resetcode=")) return line.substring(10).trim();
            }
        } catch (Exception ignored) {}
        return "";
    }

    private static String sanitizeForFolder(String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return "company";
        String cleaned = trimmed.replaceAll("[^A-Za-z0-9 ]", "_");
        return cleaned.replace(' ', '_');
    }

    void delete() {
        System.out.println("delete called");
        System.out.println("the file path is: " + path);

        try {
            if (path == null) {
                System.out.println("path is null");
                return;
            }

            File file = new File(path);
            if (file.exists()) {
                boolean deleted = file.delete();

                if (!deleted || file.exists()) {
                    System.out.println("file still exists so delete file was unsuccessful.\nPlease manually delete the file location above.");
                } else {
                    clearConsole();
                    System.out.println("-----------------------------------");
                    System.out.println("Kiosk Company Info Reset Complete");
                }
            }
        } catch (Exception ignored) {}
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
}
