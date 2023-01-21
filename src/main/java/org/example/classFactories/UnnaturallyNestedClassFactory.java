package org.example.classFactories;

import de.uni_trier.wi2.procake.data.model.base.AggregateClass;

public abstract class UnnaturallyNestedClassFactory extends ClassFactory{

    private static final String UNNATURALLY_NESTED = "XESUnnaturallyNestedClass";

    AggregateClass getBaseClass() {
        AggregateClass baseClass = super.getBaseClass();
        AggregateClass unnaturallyNested = model.getClass(UNNATURALLY_NESTED);
        if (unnaturallyNested == null) {
            unnaturallyNested = (AggregateClass) baseClass.createSubclass(UNNATURALLY_NESTED);
            unnaturallyNested.addAttribute("attributes",model.getSetSystemClass()); //TODO HIER SOLLTE STATT DER SystemSetClass EINE KLASSE HIN, DIE Sets DEFINIERT, DIE NUR OBJEKTE DER KLASSE XESBaseClass (->Erben) ENTHALTEN
            unnaturallyNested.setAbstract(true);
            unnaturallyNested.finishEditing();
        }
        return unnaturallyNested;
    }
}
