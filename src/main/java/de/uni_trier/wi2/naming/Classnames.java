package de.uni_trier.wi2.naming;

public abstract class Classnames {


    private static final String XES = "XES";
    private static final String XML = "XML";
    private static final String CLASS = "Class";

    public static final String EVENT = "Event";
    public static final String ATOMIC = "Atomic";
    public static final String STRING = "String";
    public static final String DATETIME = "DateTime";
    public static final String INTEGER = "Integer";
    public static final String FLOAT = "Float";
    public static final String BOOLEAN = "Boolean";
    public static final String ID = "ID";
    public static final String LIST = "List";
    public static final String ATTRIBUTE = "Attribute";
    public static final String ATTRIBUTE_LIST = "Attribute" + LIST;

    public static final String EVENT_CLASS = EVENT + CLASS;
    public static final String ATOMIC_CLASS = ATOMIC + CLASS;
    public static final String STRING_CLASS = STRING + CLASS;
    public static final String DATETIME_CLASS = DATETIME + CLASS;
    public static final String INTEGER_CLASS = INTEGER + CLASS;
    public static final String FLOAT_CLASS = FLOAT + CLASS;
    public static final String BOOLEAN_CLASS = BOOLEAN + CLASS;
    public static final String ID_CLASS = ID + CLASS;
    public static final String LIST_CLASS = LIST + CLASS;
    public static final String ATTRIBUTE_CLASS = ATTRIBUTE + CLASS;
    public static final String ATTRIBUTE_LIST_CLASS = ATTRIBUTE_LIST + CLASS;

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