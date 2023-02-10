package de.uni_trier.wi2.classFactories;

import de.uni_trier.wi2.namingUtils.Classnames;
import de.uni_trier.wi2.namingUtils.Postfixes;
import de.uni_trier.wi2.procake.data.model.Model;

/**
 * Factory used to create procake classes for keys with the type 'container'.
 */
public class ContainerClassFactory extends NaturallyNestedClassFactory{

    public static final String POSTFIX = Postfixes.CONTAINER;

    public ContainerClassFactory(Model model) {
        super("ContainerClass", Classnames.CONTAINER, model, model.getSetSystemClass());
    }
}
