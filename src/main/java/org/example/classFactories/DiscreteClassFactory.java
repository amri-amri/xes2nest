package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;

public class DiscreteClassFactory extends UnnaturallyNestedClassFactory{

    private AggregateClass syntaxClass;
    private final String CLASS_NAME = "XESDiscreteClass";

    public DiscreteClassFactory(Model model) {
        this.model = model;
        postfix = "DiscreteClass";
        createOrGetSyntaxClass();
    }

    private void createOrGetSyntaxClass() {
        syntaxClass = model.getClass(CLASS_NAME);
        if (syntaxClass == null) {
            syntaxClass = (AggregateClass) getBaseClass().createSubclass(CLASS_NAME);
            syntaxClass.updateAttributeType("value",model.getIntegerSystemClass());
            syntaxClass.setAbstract(false);
            syntaxClass.finishEditing();
        }
    }

    @Override
    AggregateClass getSyntaxClass() {
        return syntaxClass;
    }
}
