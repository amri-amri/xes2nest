package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;

import java.util.Map;

public class DurationClassFactory extends UnnaturallyNestedClassFactory{

    private AggregateClass syntaxClass;
    private final String CLASS_NAME = "XESDurationClass";

    public DurationClassFactory(Model model) {
        this.model = model;
        postfix = "DurationClass";
        createOrGetSyntaxClass();
    }

    private void createOrGetSyntaxClass() {
        syntaxClass = model.getClass(CLASS_NAME);
        if (syntaxClass == null) {
            syntaxClass = (AggregateClass) getBaseClass().createSubclass(CLASS_NAME);
            syntaxClass.updateAttributeType("value", model.getIntegerSystemClass());
            syntaxClass.setAbstract(false);
            syntaxClass.finishEditing();
        }
    }
    @Override
    AggregateClass getSyntaxClass() {
        return syntaxClass;
    }

    public Map<String, String> getCreatedClasses() {
        return createdClasses;
    }
}
