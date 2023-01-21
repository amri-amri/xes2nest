package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.base.AggregateClass;

public abstract class NaturallyNestedClassFactory extends ClassFactory{

    private static final String NATURALLY_NESTED = "XESUnnaturallyNestedClass";

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
