package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;

public class CollectionClassFactory extends NaturallyNestedClassFactory{

    private AggregateClass syntaxClass;
    private final String CLASS_NAME = "XESCollectionClass";

    public CollectionClassFactory(Model model) {
        this.model = model;
        postfix = "CollectionClass";
        createOrGetSyntaxClass();
    }

    private void createOrGetSyntaxClass() {
        syntaxClass = model.getClass(CLASS_NAME);
        if (syntaxClass == null) {
            syntaxClass = (AggregateClass) getBaseClass().createSubclass(CLASS_NAME);
            syntaxClass.updateAttributeType("value",model.getCollectionSystemClass());
            syntaxClass.setAbstract(false);
            syntaxClass.finishEditing();
        }
    }

    @Override
    AggregateClass getSyntaxClass() {
        return syntaxClass;
    }
}
