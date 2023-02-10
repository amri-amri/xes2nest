package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import org.example.namingUtils.Classnames;

/**
 * Factory used to create procake classes for keys with the type 'collection'.
 */
public class CollectionClassFactory extends NaturallyNestedClassFactory{

    public CollectionClassFactory(Model model) {
        super("CollectionClass", Classnames.COLLECTION, model, model.getCollectionSystemClass());
    }

}
