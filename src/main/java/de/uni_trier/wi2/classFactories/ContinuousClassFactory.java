package de.uni_trier.wi2.classFactories;

import de.uni_trier.wi2.namingUtils.Classnames;
import de.uni_trier.wi2.namingUtils.Postfixes;
import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'float'.
 */
public class ContinuousClassFactory extends UnnaturallyNestedClassFactory{

    public static final String POSTFIX = Postfixes.CONTINUOUS;

    public ContinuousClassFactory(Model model) {
        super("ContinuousClass", Classnames.CONTINUOUS, model, model.getDoubleSystemClass());
    }

}
