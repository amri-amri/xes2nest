package de.uni_trier.wi2.naming;

public abstract class KeyNameConverter {//TODO is it even possible for a valid xes to contain keys that contain whitespace???

    /**
     * Changes the key value to a valid class name by replacing whitespace with underscore.
     * Also capitalizes the Key value.
     *
     * @param key key to be changed to meet ClassName standards
     * @return valid name
     */
    public static String getValidName(String key) {
        String nKey = key.substring(0, 1).toUpperCase() + key.substring(1);
        return nKey.replace(" ", "_");
    }
}
