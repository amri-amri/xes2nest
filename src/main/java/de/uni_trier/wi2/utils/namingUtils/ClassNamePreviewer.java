package de.uni_trier.wi2.utils.namingUtils;

import de.uni_trier.wi2.utils.classFactories.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Can be used to get a List of class names that will be created when a XES file gets converted.
 */
public class ClassNamePreviewer {

    private List<String> createdClasses = new ArrayList<>();


    private String getClassOfType(String attrClassName) {
        String className;
        switch (attrClassName) {
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

    public void getDifferingClasses(Collection<String> names) {
        System.out.println("Differing Classes");
        System.out.println("Actually created but missing in Preview:");
        for (String name : names) {
            if (!createdClasses.contains(name)) System.out.println(name);
        }
        System.out.println("Existing in Preview but missing in conversion:");
        for (String name : createdClasses) {
            if (!names.contains(name)) System.out.println(name);
        }
    }
}
