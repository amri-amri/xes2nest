package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import org.example.namingUtils.Classnames;

/**
 * Factory used to create procake classes for keys with the type 'int'.
 */
public class DiscreteClassFactory extends UnnaturallyNestedClassFactory{

    public DiscreteClassFactory(Model model) {
        super("DiscreteClass", Classnames.DISCRETE, model, model.getIntegerSystemClass());
    }
}
