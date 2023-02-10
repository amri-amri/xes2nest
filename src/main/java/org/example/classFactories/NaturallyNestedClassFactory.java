package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import org.example.namingUtils.Classnames;

/**
 * Class that provides a redefined baseclass for Factories that are used to create classes for XES collection attribute types.
 * Such XES attributes are e.g. 'list' and 'container', which serve the sole purpose of having nested attributes.
 * To make use of this new base class, use inheritance.
 */
public abstract class NaturallyNestedClassFactory extends ClassFactory{

    private static final String NATURALLY_NESTED = Classnames.NATURALLY_NESTED;

    public NaturallyNestedClassFactory(String postfix, String className, Model model, DataClass dataClass) {
        super(postfix, className, model, dataClass);
    }

    /**
     * Provides a base class with only one attribute, 'value', which has a collection as a type.
     * @return a new Base Class for type classes with only one attribute.
     */
    @Override
    AggregateClass getBaseClass() {
        AggregateClass baseClass = super.getBaseClass();
        /*unnaturally nested classes (classes whose objects have key, value and attributes)
         *such object can be nested but it is not their main feature, contrary to collection objects
         *which define their value through nesting
        */
        AggregateClass naturallyNested = model.getClass(NATURALLY_NESTED);
        if (naturallyNested == null) {
            naturallyNested = (AggregateClass) baseClass.createSubclass(NATURALLY_NESTED);
            naturallyNested.updateAttributeType("value",model.getCollectionSystemClass());
            naturallyNested.setAbstract(true);
            naturallyNested.finishEditing();
        }
        return naturallyNested;
    }
}
