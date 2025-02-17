package nz.ac.auckland.se281;

import nz.ac.auckland.se281.Main.PolicyType;

import java.util.ArrayList;
import java.util.List;

public class InsuranceSystem {
    private List<ClientProfile> database;
    private ClientProfile loadedProfile;

    public InsuranceSystem() {
        this.database = new ArrayList<>();
        this.loadedProfile = null;
    }

    public void printDatabase() {
        if (database.isEmpty()) {
            MessageCli.PRINT_DB_POLICY_COUNT.printMessage("0", "s", ".");
        } else if (database.size() == 1) {
            MessageCli.PRINT_DB_POLICY_COUNT.printMessage("1", "", ":");
            MessageCli.PRINT_DB_PROFILE_HEADER_MINIMAL.printMessage("1", database.get(0).getUsername(), String.valueOf(database.get(0).getAge()));
        } else {
            MessageCli.PRINT_DB_POLICY_COUNT.printMessage(String.valueOf(database.size()), "s", ":");
            for (int i = 0; i < database.size(); i++) {
                MessageCli.PRINT_DB_PROFILE_HEADER_MINIMAL.printMessage(String.valueOf(i + 1), database.get(i).getUsername(), String.valueOf(database.get(i).getAge()));
            }
        }
    }

    public void createNewProfile(String userName, String age) {
        if (!isUniqueUsername(userName)) {
            MessageCli.INVALID_USERNAME_NOT_UNIQUE.printMessage(userName);
        } else if (userName.length() < 3) {
            MessageCli.INVALID_USERNAME_TOO_SHORT.printMessage(userName);
        } else if (!isPositiveInteger(age)) {
            MessageCli.INVALID_AGE.printMessage(age, userName);
        } else {
            String formattedUserName = toTitleCase(userName);
            int formattedAge = Integer.parseInt(age);
            ClientProfile newProfile = new ClientProfile(formattedUserName, formattedAge);
            database.add(newProfile);
            MessageCli.PROFILE_CREATED.printMessage(formattedUserName, String.valueOf(formattedAge));
        }
    }

    public void loadProfile(String userName) {
        if (loadedProfile != null) {
            MessageCli.CANNOT_CREATE_WHILE_LOADED.printMessage(loadedProfile.getUsername());
        } else {
            ClientProfile profile = findProfileByUsername(userName);
            if (profile == null) {
                MessageCli.NO_PROFILE_FOUND_TO_LOAD.printMessage(userName);
            } else {
                loadedProfile = profile;
                MessageCli.PROFILE_LOADED.printMessage(profile.getUsername());
            }
        }
    }

    public void unloadProfile() {
        if (loadedProfile == null) {
            MessageCli.NO_PROFILE_LOADED.printMessage();
        } else {
            MessageCli.PROFILE_UNLOADED.printMessage(loadedProfile.getUsername());
            loadedProfile = null;
        }
    }

    public void deleteProfile(String userName) {
        if (loadedProfile != null && loadedProfile.getUsername().equals(userName)) {
            MessageCli.CANNOT_DELETE_PROFILE_WHILE_LOADED.printMessage(userName);
        } else {
            ClientProfile profile = findProfileByUsername(userName);
            if (profile == null) {
                MessageCli.NO_PROFILE_FOUND_TO_DELETE.printMessage(userName);
            } else {
                database.remove(profile);
                MessageCli.PROFILE_DELETED.printMessage(userName);
            }
        }
    }

    public void createPolicy(PolicyType type, String[] options) {
        if (loadedProfile == null) {
            MessageCli.NO_PROFILE_FOUND_TO_CREATE_POLICY.printMessage();
        } else {
            switch (type) {
                case HOME:
                    createHomePolicy(options);
                    break;
                case CAR:
                    createCarPolicy(options);
                    break;
                case LIFE:
                    createLifePolicy(options);
                    break;
            }
        }
    }

    private void createHomePolicy(String[] options) {
        // TODO: Implement this method.
    }

    private void createCarPolicy(String[] options) {
        // TODO: Implement this method.
    }

    private void createLifePolicy(String[] options) {
        // TODO: Implement this method.
    }

    private boolean isUniqueUsername(String userName) {
        for (ClientProfile profile : database) {
            if (profile.getUsername().equalsIgnoreCase(userName)) {
                return false;
            }
        }
        return true;
    }

    private boolean isPositiveInteger(String str) {
        try {
            int num = Integer.parseInt(str);
            return num > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String toTitleCase(String str) {
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            result.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1).toLowerCase()).append(" ");
        }
        return result.toString().trim();
    }

    private ClientProfile findProfileByUsername(String userName) {
        for (ClientProfile profile : database) {
            if (profile.getUsername().equalsIgnoreCase(userName)) {
                return profile;
            }
        }
        return null;
    }
}

class ClientProfile {
    private String username;
    private int age;

    public ClientProfile(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public String getUsername() {
        return username;
    }

    public int getAge() {
        return age;
    }
}