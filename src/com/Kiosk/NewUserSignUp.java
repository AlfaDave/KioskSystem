/**
 * Main kiosk controller handling user interaction, sign-up flow, and system navigation.
 * Manages timeouts, input validation, and transitions between kiosk states.
 * Entry point for running the kiosk application.
 */
package com.Kiosk;

import jTidy.*; // << my own library why? just cause I can :)

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class NewUserSignUp {

    private List<AdvertiseTo> advertisingList = new ArrayList<>();
    private List<AdvertiseTo> proSignUpList = new ArrayList<>();
    private User me = new User();
    private UserStorage storage;
    private int timeoutSeconds = 120;
    private String resetCode = "";
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) {
        NewUserSignUp program = new NewUserSignUp();
        program.setupCompanyInfo();
        program.startProgram();
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

    private String generateResetCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int digit = (int) (Math.random() * 10);
            sb.append(digit);
        }
        return sb.toString();
    }

    private void setupCompanyInfo() {
        CompanyInfo compInfo = new CompanyInfo();

        if (!compInfo.exists()) {
            try {
                while (true) {
                    System.out.print("Enter company name: ");
                    String companyDisplayName = reader.readLine().trim();

                    System.out.println("Public display name will be: " + companyDisplayName);
                    System.out.print("Is this correct? (Yes/No): ");
                    String confirm = reader.readLine().trim().toLowerCase();

                    if (confirm.equals("y") || confirm.equals("yes")) {
                        clearConsole();
                        System.out.println("Recommended inactivity reset timeout is 120 seconds (2 minutes).");
                        System.out.print("Enter timeout in seconds (30 to 600, or blank for 120/default): ");
                        String line = reader.readLine().trim();

                        int time = 120;
                        if (!line.isEmpty()) {
                            try {
                                int value = Integer.parseInt(line);
                                if (value >= 30 && value <= 600) time = value;
                            } catch (NumberFormatException ignored) {
                            }
                        }

                        String code = generateResetCode();
                        while (true) {
                            clearConsole();
                            System.out.println("Owner reset code (for kiosk reinitialisation): " + code);
                            System.out.println("Please write this down or take a photo.");
                            System.out.print("Do you have the reset code saved? (Yes/No): ");
                            String saved = reader.readLine().trim().toLowerCase();
                            if (saved.equals("y") || saved.equals("yes")) break;
                        }
                        clearConsole();
                        System.out.println("Setup complete.");
                        System.out.println("To view your usage report at any time, type 'report123' at the welcome screen.");
                        System.out.println("Press ENTER to continue...");
                        reader.readLine();

                        compInfo = new CompanyInfo(companyDisplayName);
                        compInfo.save(companyDisplayName, time, code);
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String companyDisplayName = compInfo.loadCompany();
        timeoutSeconds = compInfo.loadTimeoutSeconds();
        resetCode = compInfo.loadResetCode();

        storage = new UserStorage(companyDisplayName);
    }

    public void startProgram() {
        while (true) {
            clearConsole();
            System.out.println("====================================");
            System.out.println("Welcome to " + storage.getCompanyDisplayName());
            System.out.println("Press ENTER to continue...");
            System.out.println("====================================");

            String input = readInput();

            if (handleReset(input)) continue;
            if (handleTestBatch(input)) continue;
            if (handleReport(input)) continue;

            clearConsole();
            Boolean wantsSignup = readYesNoWithTimeout("Would you like to sign up for a new membership?");
            if (wantsSignup == null || !wantsSignup) {
                clearConsole();
                resetUserState();
                continue;
            }

            boolean completed = welcomeNewUser();
            clearConsole();
            System.out.println(completed ? "Signup complete." : "Signup cancelled due to inactivity.");
            resetUserState();
        }
    }


    private boolean handleReset(String input) {
        if (input.equals(resetCode)) {
            clearConsole();
            CompanyInfo info = new CompanyInfo(storage.getCompanyDisplayName());
            info.delete();
            setupCompanyInfo();
            return true;
        }
        return false;
    }

    private boolean handleTestBatch(String input) {
        if (input.equalsIgnoreCase("testbatch123")) {
            clearConsole();
            runTestBatch();
            System.out.println("-----------------------------------");
            System.out.println("Automated Test Batch Complete");
            System.out.println("Press ENTER to continue...");
            readInput();
            return true;
        }
        return false;
    }

    private boolean handleReport(String input) {
        if (input.equalsIgnoreCase("report123")) {
            CompanyInfo info = new CompanyInfo();
            info.deployReportUtility();
            return true;
        }
        return false;
    }


    private String readInput() {
    String line = readLineRaw();
    if (line == null) return "";
    return line.trim();
}


private String readLineRaw() {
    try {
        return reader.readLine();
    } catch (Exception e) {
        return "";
    }
}

private boolean welcomeNewUser() {
    String firstName = readStringWithTimeout("What is your first name? ");
    if (firstName == null) return false;
    set_FirstName(jTidy.tidyString(firstName));

    String secondName = readStringWithTimeout("What is your second name? ");
    if (secondName == null) return false;
    set_SecondName(secondName);

    String email = readStringWithTimeout("What is your email address? ");
    if (email == null) return false;
    me.email = email;

    Integer age = readIntWithTimeout("What is your age? ");
    if (age == null) return false;
    set_Age(age);

    Integer genderInput = readGenderWithTimeout();
    if (genderInput == null) return false;
    set_SexInputLogic(genderInput);

    Boolean wantsPro = readYesNoWithTimeout("Would you like Pro Membership?");
    if (wantsPro == null) return false;
    me.proMember = jTidy.tidyBool(wantsPro);

    String fullName = me.firstName.get() + " " + me.secondName;

    if (wantsPro) {
        proSignUpList.add(new AdvertiseTo(fullName, me.email));
    } else {
        AddToAdvertiseProMembership(fullName, me.email);
    }

    me.active = true;
    storage.saveUser(me);
    storage.saveAdvertiseList(advertisingList);
    storage.saveProSignUpList(proSignUpList);
    return true;
}

private void resetUserState() {
    me = new User();
    advertisingList.clear();
    proSignUpList.clear();
}

private void AddToAdvertiseProMembership(String name, String email) {
    advertisingList.add(new AdvertiseTo(name, email));
}

private String readStringWithTimeout(String prompt) {
    long timeoutMillis = timeoutSeconds * 1000L;
    long start = System.currentTimeMillis();

    while (true) {
        System.out.print(prompt);
        String line = waitForLine(timeoutMillis, start);
        if (line == null) {
            clearConsole();
            return null;
        }

        String input = line.trim();
        if (!input.isEmpty()) {
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }
            clearConsole();
            return input;
        }

        System.out.println("Input cannot be empty.");
        start = System.currentTimeMillis();
    }
}

private Integer readIntWithTimeout(String prompt) {
    long timeoutMillis = timeoutSeconds * 1000L;
    long start = System.currentTimeMillis();

    while (true) {
        System.out.print(prompt);
        String line = waitForLine(timeoutMillis, start);
        if (line == null) {
            clearConsole();
            return null;
        }

        try {
            int value = Integer.parseInt(line.trim());
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }
            clearConsole();
            return value;
        } catch (NumberFormatException e) {
            System.out.println("Please enter a valid number.");
            start = System.currentTimeMillis();
        }
    }
}

private Integer readGenderWithTimeout() {
    long timeoutMillis = timeoutSeconds * 1000L;
    long start = System.currentTimeMillis();

    while (true) {
        System.out.print("Are you Male = 1, Female = 2, Other = 3: ");
        String line = waitForLine(timeoutMillis, start);
        if (line == null) {
            clearConsole();
            return null;
        }

        try {
            int value = Integer.parseInt(line.trim());
            if (value >= 1 && value <= 3) {
                try {
                    Thread.sleep(50);
                } catch (Exception ignored) {
                }
                clearConsole();
                return value;
            }
        } catch (NumberFormatException ignored) {
        }

        System.out.println("Please enter 1, 2, or 3.");
        start = System.currentTimeMillis();
    }
}

private Boolean readYesNoWithTimeout(String prompt) {
    long timeoutMillis = timeoutSeconds * 1000L;
    long start = System.currentTimeMillis();

    while (true) {
        System.out.print(prompt + " (Yes/No): ");
        String line = waitForLine(timeoutMillis, start);
        if (line == null) {
            clearConsole();
            return null;
        }

        String input = line.trim().toLowerCase();
        if (input.equals("y") || input.equals("yes")) {
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }
            clearConsole();
            return true;
        }
        if (input.equals("n") || input.equals("no")) {
            try {
                Thread.sleep(50);
            } catch (Exception ignored) {
            }
            clearConsole();
            return false;
        }

        System.out.println("Please enter Yes or No.");
        start = System.currentTimeMillis();
    }
}

private String waitForLine(long timeoutMillis, long start) {
    try {
        while (true) {
            if (reader.ready()) {
                return reader.readLine();
            }
            if (System.currentTimeMillis() - start > timeoutMillis) {
                return null;
            }
            Thread.sleep(200);
        }
    } catch (Exception e) {
        return null;
    }
}

private void set_FirstName(stringj name) {
    me.firstName = name;
}

private void set_SecondName(String name) {
    me.secondName = name;
}

private void set_Age(int value) {
    me.age = value;
}

private void set_Sex(User.Sex value) {
    me.sex = value;
}

private void set_SexInputLogic(int value) {
    me.sex = (value == 1) ? User.Sex.Male :
            (value == 2) ? User.Sex.Female :
                    User.Sex.Other;
}

private void set_Active(Boolean value) {
    me.active = value;
}

private void set_ProMember(boolj value) {
    me.proMember = value;
}

private void runTestBatch() {
    System.out.println("Running automated test batch...");

    String[] firstNames = {"Alice", "Bob", "Charlie", "Diana", "Ethan", "Fiona"};
    String[] lastNames = {"Smith", "Jones", "Taylor", "Brown", "Wilson", "Clark"};
    String[] emails = {
            "alice@example.com", "bob@example.com", "charlie@example.com",
            "diana@example.com", "ethan@example.com", "fiona@example.com"
    };

    advertisingList.clear();
    proSignUpList.clear();

    for (int i = 0; i < 20; i++) {

        User batchUser = new User();
        batchUser.firstName = jTidy.tidyString(firstNames[i % firstNames.length]);
        batchUser.secondName = lastNames[i % lastNames.length];
        batchUser.email = emails[i % emails.length];

        batchUser.age = 18 + (i % 40);

        batchUser.sex = (i % 3 == 0) ? User.Sex.Male :
                (i % 3 == 1) ? User.Sex.Female :
                        User.Sex.Other;

        boolean wantsPro = (i % 2 == 0);
        batchUser.proMember = jTidy.tidyBool(wantsPro);

        String fullName = batchUser.firstName.get() + " " + batchUser.secondName;

        if (wantsPro) {
            proSignUpList.add(new AdvertiseTo(fullName, batchUser.email));
        } else {
            advertisingList.add(new AdvertiseTo(fullName, batchUser.email));
        }

        batchUser.active = true;

        storage.saveUser(batchUser);
    }

    storage.saveAdvertiseList(advertisingList);
    storage.saveProSignUpList(proSignUpList);

    System.out.println("20 test users created.");
}


}
