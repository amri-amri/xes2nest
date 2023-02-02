package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'string'.
 */
public class LiteralClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = "LiteralClass";

    public LiteralClassFactory(Model model) {
        super(POSTFIX,"XESLiteralClass", model, model.getStringSystemClass());
    }

}
