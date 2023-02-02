package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'collection'.
 */
public class CollectionClassFactory extends NaturallyNestedClassFactory{

    public static final String POSTFIX = "CollectionClass";

    public CollectionClassFactory(Model model) {
        super(POSTFIX,"XESCollectionClass", model, model.getCollectionSystemClass());
    }

}
