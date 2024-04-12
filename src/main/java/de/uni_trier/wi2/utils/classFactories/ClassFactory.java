package de.uni_trier.wi2.utils.classFactories;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import de.uni_trier.wi2.utils.namingUtils.KeyNameConverter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class used to add Classes to the provided ProCake-Model that resemble the key of a XES Attribute.
 * After creation, a key class can then be used to create objects of this class, or to implement similarity measures for the key the class matches.
 */
public abstract class ClassFactory {

    /**
     * Model to which the key classes should be added
     */
    final Model model;

    /**
     * String that the factory appends to the key to create a name of the new key class.
     * Should be closely related to the {@link ClassFactory#type_CLASS_NAME}.
     */
    private final String POSTFIX;

    /**
     * Name of the class that serves as a basis for the creation of key classes.
     * The type class normally resembles the type of the attribute for which a key class is created.
     */
    private final String type_CLASS_NAME;
    private AggregateClass typeClass;


    /**
     * Map of all classes that were created by the factory.
     * The keys of the entries are the key values of the XES attribute, the values of the entries are the names of the resembling ProCake DataClasses.
     */
    private final Map<String, String> createdClassNames = new HashMap<>();

    private final String BASE = Classnames.BASE;


    private ClassFactory(String postfix, String className, Model model) {
        this.model = model;
        this.POSTFIX = postfix;
        this.type_CLASS_NAME = className;
    }

    /**
     * Creates a new ClassFactory.
     * @param postfix string that is appended to new key classes.
     * @param className name of the class that should be used as basis for the creation of new key classes.
     * @param model ProCake model to which the key classes should be added
     * @param dataClass Class to which the "value" attribute of the base class should be changed.
     */
    ClassFactory(String postfix, String className, Model model, DataClass dataClass) {
        this(postfix, className, model);
        Map<String, DataClass> updates = new HashMap<>();
        updates.put("value", dataClass);
        initializeTypeClass(updates);
    }

    /**
     * Gets the key class of the given key in the context of the factory's type class from the ProCake-Model of the factory.
     * If the key class does not exist yet, a new key class based on the type class of the factory is created and then returned.
     * @param keyName key of the needed key class.
     * @return the matching ProCake Model class.
     */
    public AggregateClass getClass(String keyName) {
        String classname = KeyNameConverter.getValidName(keyName) + POSTFIX;
        if (model.getClass(classname) != null) return model.getClass(classname);
        AggregateClass nClass = (AggregateClass) typeClass.createSubclass(classname);
        nClass.setAbstract(false);
        nClass.finishEditing();
        createdClassNames.put(keyName, classname);
        return nClass;
    }

    /**
     * Provides a ProCake Class with a specific structure that then can be used to create specific Procake type classes.
     * @return class which provides the overall structure of the type class it is used for to build.
     */
    AggregateClass getBaseClass() {
        AggregateClass baseClass = model.getClass(BASE);
        if (baseClass == null) {
            baseClass = (AggregateClass) model.getAggregateSystemClass().createSubclass(BASE);
            baseClass.addAttribute("key",model.getStringSystemClass());
            baseClass.addAttribute("value",model.getDataSystemClass());
            baseClass.setAbstract(true);
            baseClass.finishEditing();
        }
        return baseClass;
    }

    /**
     * initializes the {@link ClassFactory#typeClass} and should therefore be called in the constructor.
     * The user has the possibility to update the attributes of the baseClass to classes that fit the specific use case.
      * @param updates updates that should be performed with the attribute name as key and the new type of the attribute as value.
     */
    private void initializeTypeClass(Map<String, DataClass> updates) {
        typeClass = model.getClass(type_CLASS_NAME);
        if (typeClass == null) {
            typeClass = (AggregateClass) getBaseClass().createSubclass(type_CLASS_NAME);
            for (Entry<String, DataClass> update: updates.entrySet()) {
                typeClass.updateAttributeType(update.getKey(), update.getValue());
            }
            typeClass.setAbstract(true);
            typeClass.finishEditing();
        }
    }

    /**
     * Returns an unmodifiable map of the names of the classes created by the factory as values and the matching keys as map keys.
     * @return unmodifiable map of all the classes created by the factory.
     */
    public Map<String, String> getNamesOfCreatedClasses() {
        return Map.copyOf(createdClassNames);
    }
}
