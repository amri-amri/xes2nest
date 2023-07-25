package de.uni_trier.wi2.namingUtils;

import de.uni_trier.wi2.classFactories.*;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Can be used to get a List of class names that will be created when a XES file gets converted.
 */
public class ClassNamePreviewer {

    private List<String> createdClasses = new ArrayList<>();
    final private XLog log;

    /**
     * Creates a new Class name previewer.
     * @param filepath path to the xes file, from which the XES-files shall be abstracted
     * @throws Exception
     */
    public ClassNamePreviewer(String filepath) throws Exception {
        XFactoryNaiveImpl xFactory = new XFactoryNaiveImpl();
        XesXmlParser xmlParser = new XesXmlParser(xFactory);
        log = xmlParser.parse(new File(filepath)).get(0);
    }

    /**
     * Prints the class names of the classes that will be created when converting the XES file a NEST Graph.
     */
    public void printClassNames() {
        for (XAttribute attribute : log.getGlobalEventAttributes()) {
            printAttribute(attribute);
        }
        for (XAttribute attribute : log.getGlobalTraceAttributes()) {
            printAttribute(attribute);
        }
        for( int i = 0; i < log.size(); i++) {
            XTrace xTrace = log.get(i);
            printAttributeMap(xTrace.getAttributes());
            for(int j = 0; j < xTrace.size(); j++) {
                XEvent xEvent = xTrace.get(j);
                printAttributeMap(xEvent.getAttributes());
            }
        }
    }

    private void printAttributeMap(XAttributeMap attributes) {
        for (XAttribute current: attributes.values()) {
            printAttribute(current);
        }
    }

    private void printAttribute(XAttribute attribute) {
        String className = createClassName(attribute);
        if (!createdClasses.contains(className)) {
            createdClasses.add(className);
            System.out.println(attribute.getKey() + " -> " + className);
        }
        printAttributeMap(attribute.getAttributes());

    }

    private String createClassName(XAttribute attr) {
        return KeyNameConverter.getValidName(attr.getKey()) + getClassOfType(attr.getClass().getSimpleName());
    }

    private String getClassOfType(String attrClassName) {
        String className;
        switch(attrClassName){
            case "XAttributeLiteralImpl":
                className = LiteralClassFactory.POSTFIX;
                break;
            case "XAttributeBooleanImpl":
                className = BooleanClassFactory.POSTFIX;
                break;
            case "XAttributeContinuousImpl":
                className = ContinuousClassFactory.POSTFIX;
                break;
            case "XAttributeDiscreteImpl":
                className = DiscreteClassFactory.POSTFIX;
                break;
            case "XAttributeTimestampImpl":
                className = TimestampClassFactory.POSTFIX;
                break;
            case "XAttributeDurationImpl":
                className = DurationClassFactory.POSTFIX;
                break;
            case "XAttributeIDImpl":
                className = IDClassFactory.POSTFIX;
                break;
            case "XAttributeCollectionImpl":
                className = CollectionClassFactory.POSTFIX;
                break;
            case "XAttributeContainerImpl":
                className = ContainerClassFactory.POSTFIX;
                break;
            case "XAttributeListImpl":
                className = ListClassFactory.POSTFIX;
                break;
            default:
                className = "UnknownClass";
        }
        return className;
    }

    /**
     * Prints the names of the classes that were predicted by preview and which do not exist in "names" and vice versa.
     * {@link ClassNamePreviewer#printClassNames()} must be run before using the method.
     * @param names Class names to which the preview should be compared.
     */
    public void getDifferingClasses(Collection<String> names) {
        System.out.println("Differing Classes");
        System.out.println("Actually created but missing in Preview:");
        for (String name : names) {
            if (!createdClasses.contains(name)) System.out.println(name);
        }
        System.out.println("Existing in Preview but missing in conversion:");
        for ( String name :createdClasses) {
            if (!names.contains(name)) System.out.println(name);
        }
    }
}
