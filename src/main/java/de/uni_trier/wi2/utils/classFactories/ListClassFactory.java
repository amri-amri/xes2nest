package de.uni_trier.wi2.utils.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import de.uni_trier.wi2.utils.namingUtils.Postfixes;

/**
 * Factory used to create procake classes for keys with the type 'list'.
 */
public class ListClassFactory extends NaturallyNestedClassFactory{

    public static final String POSTFIX = Postfixes.LIST;

    public ListClassFactory(Model model) {
        super("ListClass", Classnames.LIST, model, model.getListSystemClass());
    }
}
