package org.example.utils;

public class KeyNameConverter { //TODO Come up with naming conventions
    /**
     * Changes the key value to a valid class name by replacing : with underscore.
     * Also capitalizes the Key value.
     * @param key key to should be changed to meet ClassName standards
     * @return valid name
     */
    public static String getValidName(String key) {
        String nKey = key.substring(0,1).toUpperCase() + key.substring(1);
        return nKey.replace(":", "_");
    }
}
