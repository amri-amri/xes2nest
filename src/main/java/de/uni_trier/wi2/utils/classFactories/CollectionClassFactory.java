package de.uni_trier.wi2.utils.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import de.uni_trier.wi2.utils.namingUtils.Suffixes;

/**
 * Factory used to create procake classes for keys with the type 'collection'.
 */
public class CollectionClassFactory extends NaturallyNestedClassFactory{

    public static final String POSTFIX = Suffixes.COLLECTION;

    public CollectionClassFactory(Model model) {
        super("CollectionClass", Classnames.COLLECTION, model, model.getCollectionSystemClass());
    }

}
