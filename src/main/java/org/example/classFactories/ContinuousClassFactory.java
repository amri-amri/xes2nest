package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;

public class ContinuousClassFactory extends UnnaturallyNestedClassFactory{

    private AggregateClass syntaxClass;
    private final String CLASS_NAME = "XESContinuousClass";

    public ContinuousClassFactory(Model model) {
        this.model = model;
        postfix = "ContinuousClass";
        createOrGetSyntaxClass();
    }

    private void createOrGetSyntaxClass() {
        syntaxClass = model.getClass(CLASS_NAME);
        if (syntaxClass == null) {
            syntaxClass = (AggregateClass) getBaseClass().createSubclass(CLASS_NAME);
            syntaxClass.updateAttributeType("value",model.getDoubleSystemClass());
            syntaxClass.setAbstract(false);
            syntaxClass.finishEditing();
        }
    }

    @Override
    AggregateClass getSyntaxClass() {
        return syntaxClass;
    }
}
