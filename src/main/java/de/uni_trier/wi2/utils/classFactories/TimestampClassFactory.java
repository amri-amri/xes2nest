package de.uni_trier.wi2.utils.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import de.uni_trier.wi2.utils.namingUtils.Postfixes;

/**
 * Factory used to create procake classes for keys with the type 'date'.
 */
public class TimestampClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = Postfixes.TIMESTAMP;

    public TimestampClassFactory(Model model) {
        super("TimestampClass", Classnames.TIMESTAMP, model, model.getTimestampSystemClass());
    }
}
