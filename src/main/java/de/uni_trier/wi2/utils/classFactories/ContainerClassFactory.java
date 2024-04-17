package de.uni_trier.wi2.utils.classFactories;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import de.uni_trier.wi2.utils.namingUtils.Suffixes;

/**
 * Factory used to create procake classes for keys with the type 'container'.
 */
public class ContainerClassFactory extends NaturallyNestedClassFactory{

    public static final String POSTFIX = Suffixes.CONTAINER;

    public ContainerClassFactory(Model model) {
        super("ContainerClass", Classnames.CONTAINER, model, model.getSetSystemClass());
    }
}
