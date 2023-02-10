package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import org.example.namingUtils.Classnames;

/**
 * Factory used to create procake classes for keys with the type 'boolean'.
 */
public class BooleanClassFactory extends UnnaturallyNestedClassFactory{

    public BooleanClassFactory(Model model) {
        super("BooleanClass", Classnames.BOOLEAN, model, model.getBooleanSystemClass());
    }

}
