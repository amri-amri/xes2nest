package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import org.example.namingUtils.Classnames;

/**
 * Factory used to create procake classes for keys with the type 'duration'.
 */
public class DurationClassFactory extends UnnaturallyNestedClassFactory{

    public DurationClassFactory(Model model) {
        super("DurationClass", Classnames.DURATION, model, model.getIntegerSystemClass());
    }
}
