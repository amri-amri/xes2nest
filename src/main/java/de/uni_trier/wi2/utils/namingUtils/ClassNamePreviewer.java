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
                className = LiteralClassFactory.SUFFIX;
                break;
            case "XAttributeBooleanImpl":
                className = BooleanClassFactory.SUFFIX;
                break;
            case "XAttributeContinuousImpl":
                className = ContinuousClassFactory.SUFFIX;
                break;
            case "XAttributeDiscreteImpl":
                className = DiscreteClassFactory.SUFFIX;
                break;
            case "XAttributeTimestampImpl":
                className = TimestampClassFactory.SUFFIX;
                break;
            case "XAttributeDurationImpl":
                className = DurationClassFactory.SUFFIX;
                break;
            case "XAttributeIDImpl":
                className = IDClassFactory.SUFFIX;
                break;
            case "XAttributeCollectionImpl":
                className = CollectionClassFactory.SUFFIX;
                break;
            case "XAttributeContainerImpl":
                className = ContainerClassFactory.SUFFIX;
                break;
            case "XAttributeListImpl":
                className = ListClassFactory.SUFFIX;
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
