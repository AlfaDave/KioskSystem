/**
 * Handles saving and loading user data, advertising lists, and Pro membership signups.
 * Writes structured .data files used by the kiosk and reporting system.
 * Ensures persistent storage inside the company’s folder structure.
 */

package com.Kiosk;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class UserStorage {

    private final String companyDisplayName;
    private final String safeCompanyName;

    public final String companyRoot;
    private final String baseFolder;
    private final String signUpsPath;
    private final String advertisePath;
    private final String proSignUpPath;

    UserStorage(String companyDisplayName) {
        this.companyDisplayName = companyDisplayName;
        this.safeCompanyName = sanitizeForFolder(companyDisplayName);

        companyRoot = System.getProperty("user.home")
                + "\\Documents\\" + safeCompanyName + "\\";
        baseFolder = companyRoot + "NewUserSignUps\\";
        signUpsPath = baseFolder + "SignUps.data";
        advertisePath = baseFolder + "advertise.data";
        proSignUpPath = baseFolder + "prosignups.data";
    }

    String getCompanyDisplayName() {
        return companyDisplayName;
    }

    String getSafeCompanyName() {
        return safeCompanyName;
    }

    private static String sanitizeForFolder(String name) {
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return "company";
        String cleaned = trimmed.replaceAll("[^A-Za-z0-9 ]", "_");
        return cleaned.replace(' ', '_');
    }

    private void ensureFileExists(String path) {
        try {
            File file = new File(path);
            file.getParentFile().mkdirs();
            if (!file.exists()) file.createNewFile();
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    void saveUser(User user) {
        ensureFileExists(signUpsPath);
        try (FileWriter writer = new FileWriter(signUpsPath, true)) {
            writer.write("====================================\n");
            writer.write("First Name: " + user.firstName.get() + "\n");
            writer.write("Second Name: " + user.secondName + "\n");
            writer.write("Email: " + user.email + "\n");
            writer.write("Age: " + user.age + "\n");
            writer.write("Sex: " + user.sex + "\n");
            writer.write("Pro Member: " + user.proMember.get() + "\n");
            writer.write("Active: " + user.active + "\n");
            writer.write("====================================\n\n");
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    void saveAdvertiseList(List<AdvertiseTo> list) {
        ensureFileExists(advertisePath);
        try (FileWriter writer = new FileWriter(advertisePath, true)) {
            for (AdvertiseTo accounts : list) {
                writer.write(accounts.name + " : " + accounts.email + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void saveProSignUpList(List<AdvertiseTo> list) {
        ensureFileExists(proSignUpPath);
        try (FileWriter writer = new FileWriter(proSignUpPath, true)) {
            for (AdvertiseTo acounts : list) {
                writer.write(acounts.name + " : " + acounts.email + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
