package de.uni_trier.wi2.utils.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import de.uni_trier.wi2.utils.namingUtils.Suffixes;

/**
 * Factory used to create procake classes for keys with the type 'boolean'.
 */
public class BooleanClassFactory extends UnnaturallyNestedClassFactory{

    public static final String SUFFIX = Suffixes.BOOLEAN;

    public BooleanClassFactory(Model model) {
        super("BooleanClass", Classnames.BOOLEAN, model, model.getBooleanSystemClass());
    }

}
