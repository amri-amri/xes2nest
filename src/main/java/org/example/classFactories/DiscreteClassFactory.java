package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'int'.
 */
public class DiscreteClassFactory extends UnnaturallyNestedClassFactory{

    public DiscreteClassFactory(Model model) {
        super("DiscreteClass","XESDiscreteClass", model, model.getIntegerSystemClass());
    }
}
