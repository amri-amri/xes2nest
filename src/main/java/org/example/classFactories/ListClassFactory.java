package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import org.example.namingUtils.Classnames;

/**
 * Factory used to create procake classes for keys with the type 'list'.
 */
public class ListClassFactory extends NaturallyNestedClassFactory{

    public ListClassFactory(Model model) {
        super("ListClass", Classnames.LIST, model, model.getListSystemClass());
    }
}
