package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;

public class TimestampClassFactory extends UnnaturallyNestedClassFactory{

    private AggregateClass syntaxClass;
    private final String CLASS_NAME = "XESTimestampClass";

    public TimestampClassFactory(Model model) {
        this.model = model;
        postfix = "TimestampClass";
        createOrGetSyntaxClass();
    }
    private void createOrGetSyntaxClass() {
        syntaxClass = model.getClass(CLASS_NAME);
        if (syntaxClass == null) {
            syntaxClass = (AggregateClass) getBaseClass().createSubclass(CLASS_NAME);
            syntaxClass.updateAttributeType("value",model.getTimestampSystemClass());
            syntaxClass.setAbstract(false);
            syntaxClass.finishEditing();
        }
    }
    @Override
    AggregateClass getSyntaxClass() {
        return syntaxClass;
    }
}
