package de.uni_trier.wi2.classFactories;

import de.uni_trier.wi2.namingUtils.Classnames;
import de.uni_trier.wi2.namingUtils.Postfixes;
import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'string'.
 */
public class LiteralClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = Postfixes.LITERAL;

    public LiteralClassFactory(Model model) {
        super("LiteralClass", Classnames.LITERAL, model, model.getStringSystemClass());
    }

}
