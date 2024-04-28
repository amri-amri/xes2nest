package de.uni_trier.wi2.naming;

public abstract class Classnames {


    private static final String XES = "XES";
    private static final String XML = "XML";
    private static final String CLASS = "Class";

    public static final String EVENT = "Event" + CLASS;
    public static final String ATOMIC = "Atomic" + CLASS;
    public static final String STRING = "String" + CLASS;
    public static final String DATETIME = "DateTime" + CLASS;
    public static final String INTEGER = "Integer" + CLASS;
    public static final String FLOAT = "Float" + CLASS;
    public static final String BOOLEAN = "Boolean" + CLASS;
    public static final String ID = "ID" + CLASS;
    public static final String LIST = "List" + CLASS;
    public static final String ATTRIBUTE = "Attribute" + CLASS;
    public static final String ATTRIBUTELIST = "Attribute" + LIST + CLASS;

    public static String getXESClassName(String classnameSuffix) {
        return XES + classnameSuffix;
    }
    public static String getXMLClassName(String classnameSuffix) {
        return XML + classnameSuffix;
    }

    public static String getXESSubClassName(String key, String classnameSuffix) {
        return XES + KeyNameConverter.getValidName(key) + classnameSuffix;
    }


}