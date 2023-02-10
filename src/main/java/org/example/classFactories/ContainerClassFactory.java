package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import org.example.namingUtils.Classnames;

/**
 * Factory used to create procake classes for keys with the type 'container'.
 */
public class ContainerClassFactory extends NaturallyNestedClassFactory{

    public ContainerClassFactory(Model model) {
        super("ContainerClass", Classnames.CONTAINER, model, model.getSetSystemClass());
    }
}
