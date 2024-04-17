package de.uni_trier.wi2.utils.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import de.uni_trier.wi2.utils.namingUtils.Suffixes;

/**
 * Factory used to create procake classes for keys with the type 'string'.
 */
public class LiteralClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = Suffixes.LITERAL;

    public LiteralClassFactory(Model model) {
        super("LiteralClass", Classnames.LITERAL, model, model.getStringSystemClass());
    }

}
