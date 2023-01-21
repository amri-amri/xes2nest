package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.procake.data.model.Model;
import org.example.utils.KeyNameConverter;

import java.util.HashMap;
import java.util.Map;

public abstract class ClassFactory {

    Model model;
    String postfix;

    final Map<String, String> createdClasses = new HashMap<>();

    private final String BASE = "XESBaseClass";

    public AggregateClass getClass(String keyName) {
        String classname = KeyNameConverter.getValidName(keyName) + postfix;
        if (createdClasses.put(keyName, classname) != null) return model.getClass(classname);
        AggregateClass nClass = (AggregateClass) getSyntaxClass().createSubclass(classname);
        nClass.setAbstract(false);
        nClass.finishEditing();
        return nClass;
    }

    AggregateClass getBaseClass() {
        AggregateClass baseClass = model.getClass(BASE);
        if (baseClass == null) {
            baseClass = (AggregateClass) model.getAggregateSystemClass().createSubclass(BASE);
            baseClass.addAttribute("key",model.getStringSystemClass());
            baseClass.addAttribute("value",model.getDataSystemClass());
            baseClass.setAbstract(true);
            baseClass.finishEditing();
        }
        return baseClass;
    }

    public Map<String, String> getCreatedClasses() {
        return createdClasses;
    }
    abstract AggregateClass getSyntaxClass();
}
