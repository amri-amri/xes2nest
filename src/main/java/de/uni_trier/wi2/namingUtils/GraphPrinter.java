package org.example.utils;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.model.*;
import org.example.XESGraph;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

public class GraphPrinter {

    public void print(XESGraph graph) {
        println("Graph:\n");
        printGraphAttributes(graph);
        printGraphExtensions(graph);
        printEvents(graph);

    }

    private void printXAttributes(XAttributable o) {
        XAttributeMap attributes = o.getAttributes();
        Set<String> attributeKeys = attributes.keySet();
        for (String key : attributeKeys) {
            XAttribute attribute = attributes.get(key);
            printAttribute(attribute);
        }
    }

    private void printNaturallyUnnested(XAttribute o) {
        String className = reducedClassname(o.getClass().getSimpleName());
        Method getVal;
        try {
            getVal = o.getClass().getMethod("getValue");
            println(className + " > " + o.getKey() + " > " + getVal.invoke(o));
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            println(className + " > " + o.getKey());
        }
        printXAttributes(o);
    }

    private void printNaturallyNested(XAttributeCollection o) {
        String className = new StringBuilder(new StringBuilder(o.getClass().getSimpleName().substring(10)).reverse().substring(4)).reverse().toString();
        println(className + " > " + o.getKey() + " >");

        Collection<XAttribute> listCollection = o.getCollection();
        for (XAttribute colEntry : listCollection) {
            printAttribute(colEntry);
        }
    }

    private void printAttribute(XAttribute attribute) {
        if (attribute instanceof XAttributeCollection) {
            printNaturallyNested((XAttributeCollection) attribute);
        } else {
            printNaturallyUnnested(attribute);
        }

    }

    private void printXExtension(XExtension o) {
        println(o.getName());
    }

    private void printEvents(XESGraph graph) {
        for (XEvent event: graph.getNodes()) {
            println("E");
            printXAttributes(event);
        }
    }
    private void printGraphAttributes(XESGraph graph) {
        println("Graph Attributes:");
        printXAttributes(graph);
    }

    private void printGraphExtensions(XESGraph graph) {
        println("Graph Extensions:");
        for (XExtension extension: graph.getExtensions()) {
            printXExtension(extension);
        }
    }

    private void println(String str) {
        System.out.println(str);
    }

    private String reducedClassname(String classname) {
        return new StringBuilder(new StringBuilder(classname.substring(10)).reverse().substring(4)).reverse().toString();
    }
}
