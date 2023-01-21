package org.example.utils;

public class KeyNameConverter {
    /**
     * Changes the key value to a valid class name by replacing : with underscore.
     * Also capitalizes the Key value.
     * @param key key to be made conformant to ClassName standards
     * @return valid name
     */
    public static String getValidName(String key) {
        String nkey = key.substring(0,1).toUpperCase() + key.substring(1);
        return nkey.replace(":", "");
    }
}
