package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import org.example.namingUtils.Classnames;

/**
 * Factory used to create procake classes for keys with the type 'string'.
 */
public class LiteralClassFactory extends UnnaturallyNestedClassFactory{

    public LiteralClassFactory(Model model) {
        super("LiteralClass", Classnames.LITERAL, model, model.getStringSystemClass());
    }

}
