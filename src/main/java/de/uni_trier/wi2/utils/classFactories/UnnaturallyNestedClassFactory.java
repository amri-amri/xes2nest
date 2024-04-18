package de.uni_trier.wi2.utils.classFactories;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.utils.namingUtils.Classnames;

/**
 * Class that provides a redefined baseclass for Factories that are used to create classes for atomic XES attribute types.
 * Such XES attributes are e.g. 'string' and 'date', which have a specific value, but also can have nested attributes.
 * To make use of this new base class, use inheritance.
 */
public abstract class UnnaturallyNestedClassFactory extends ClassFactory{

    private static final String UNNATURALLY_NESTED = Classnames.UNNATURALLY_NESTED;

    public UnnaturallyNestedClassFactory(String suffix, String className, Model model, DataClass dataClass) {
        super(suffix, className, model, dataClass);
    }

    /**
     * Provides a base class with only two attributes.
     * The attribute 'value', should be set in the type class to whatever atomic type the type class represents.
     * the attribute 'attributes' has a set as a type.
     * @return a new Base Class for type classes that represent a atomic type with possibly nested attributes.
     */
    AggregateClass getBaseClass() {
        AggregateClass baseClass = super.getBaseClass();
        AggregateClass unnaturallyNested = model.getClass(UNNATURALLY_NESTED);
        if (unnaturallyNested == null) {
            unnaturallyNested = (AggregateClass) baseClass.createSubclass(UNNATURALLY_NESTED);
            unnaturallyNested.addAttribute("attributes",model.getSetSystemClass()); //TODO: HIER SOLLTE STATT DER SystemSetClass EINE KLASSE HIN, DIE Sets DEFINIERT, DIE NUR OBJEKTE DER KLASSE XESBaseClass (->Erben) ENTHALTEN
            unnaturallyNested.setAbstract(true);
            unnaturallyNested.finishEditing();
        }
        return unnaturallyNested;
    }
}
