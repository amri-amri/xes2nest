package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import org.example.namingUtils.Classnames;

/**
 * Factory used to create procake classes for keys with the type 'float'.
 */
public class ContinuousClassFactory extends UnnaturallyNestedClassFactory{

    public ContinuousClassFactory(Model model) {
        super("ContinuousClass", Classnames.CONTINUOUS, model, model.getDoubleSystemClass());
    }

}
