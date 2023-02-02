package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'boolean'.
 */
public class BooleanClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = "BooleanClass";

    public BooleanClassFactory(Model model) {
        super(POSTFIX,"XESBooleanClass", model, model.getBooleanSystemClass());
    }

}
