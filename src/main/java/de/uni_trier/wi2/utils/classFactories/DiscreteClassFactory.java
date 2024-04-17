package de.uni_trier.wi2.utils.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import de.uni_trier.wi2.utils.namingUtils.Suffixes;

/**
 * Factory used to create procake classes for keys with the type 'int'.
 */
public class DiscreteClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = Suffixes.DISCRETE;

    public DiscreteClassFactory(Model model) {
        super("DiscreteClass", Classnames.DISCRETE, model, model.getIntegerSystemClass());
    }
}
