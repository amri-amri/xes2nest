package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'id'.
 */
public class IDClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = "IDClass";

    public IDClassFactory(Model model) {
        super(POSTFIX,"XESIDClass", model, model.getStringSystemClass());
    }
}
