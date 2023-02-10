package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import org.example.namingUtils.Classnames;

/**
 * Factory used to create procake classes for keys with the type 'date'.
 */
public class TimestampClassFactory extends UnnaturallyNestedClassFactory{

    public TimestampClassFactory(Model model) {
        super("TimestampClass", Classnames.TIMESTAMP, model, model.getTimestampSystemClass());
    }
}
