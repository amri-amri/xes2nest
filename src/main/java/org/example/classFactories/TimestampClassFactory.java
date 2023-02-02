package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'date'.
 */
public class TimestampClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = "TimestampClass";

    public TimestampClassFactory(Model model) {
        super(POSTFIX,"XESTimestampClass", model, model.getTimestampSystemClass());
    }
}
