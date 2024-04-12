package de.uni_trier.wi2.utils.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import de.uni_trier.wi2.utils.namingUtils.Postfixes;

/**
 * Factory used to create procake classes for keys with the type 'duration'.
 */
public class DurationClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = Postfixes.DURATION;

    public DurationClassFactory(Model model) {
        super("DurationClass", Classnames.DURATION, model, model.getIntegerSystemClass());
    }
}
