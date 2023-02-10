package de.uni_trier.wi2.classFactories;

import de.uni_trier.wi2.namingUtils.Postfixes;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.namingUtils.Classnames;

/**
 * Factory used to create procake classes for keys with the type 'int'.
 */
public class DiscreteClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = Postfixes.DISCRETE;

    public DiscreteClassFactory(Model model) {
        super("DiscreteClass", Classnames.DISCRETE, model, model.getIntegerSystemClass());
    }
}
