package de.uni_trier.wi2.utils.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import de.uni_trier.wi2.utils.namingUtils.Suffixes;

/**
 * Factory used to create procake classes for keys with the type 'id'.
 */
public class IDClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = Suffixes.ID;

    public IDClassFactory(Model model) {
        super("IDClass", Classnames.ID, model, model.getStringSystemClass());
    }
}
