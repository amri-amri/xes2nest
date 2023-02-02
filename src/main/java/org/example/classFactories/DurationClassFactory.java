package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'duration'.
 */
public class DurationClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = "DurationClass";

    public DurationClassFactory(Model model) {
        super(POSTFIX,"XESDurationClass", model, model.getIntegerSystemClass());
    }
}
