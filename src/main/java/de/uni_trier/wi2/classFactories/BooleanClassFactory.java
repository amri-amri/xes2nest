package de.uni_trier.wi2.classFactories;

import de.uni_trier.wi2.namingUtils.Classnames;
import de.uni_trier.wi2.namingUtils.Postfixes;
import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'boolean'.
 */
public class BooleanClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = Postfixes.BOOLEAN;

    public BooleanClassFactory(Model model) {
        super("BooleanClass", Classnames.BOOLEAN, model, model.getBooleanSystemClass());
    }

}
