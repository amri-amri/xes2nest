package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'int'.
 */
public class DiscreteClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = "DiscreteClass";

    public DiscreteClassFactory(Model model) {
        super(POSTFIX,"XESDiscreteClass", model, model.getIntegerSystemClass());
    }
}
