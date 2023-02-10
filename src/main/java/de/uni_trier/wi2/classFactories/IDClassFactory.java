package de.uni_trier.wi2.classFactories;

import de.uni_trier.wi2.namingUtils.Classnames;
import de.uni_trier.wi2.namingUtils.Postfixes;
import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'id'.
 */
public class IDClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = Postfixes.ID;

    public IDClassFactory(Model model) {
        super("IDClass", Classnames.ID, model, model.getStringSystemClass());
    }
}
