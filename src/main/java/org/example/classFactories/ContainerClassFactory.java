package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'container'.
 */
public class ContainerClassFactory extends NaturallyNestedClassFactory{

    public static final String POSTFIX = "ContainerClass";

    public ContainerClassFactory(Model model) {
        super(POSTFIX,"XESContainerClass", model, model.getSetSystemClass());
    }
}
