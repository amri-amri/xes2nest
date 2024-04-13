package de.uni_trier.wi2.conversion;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.procake.data.model.base.SetClass;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.utils.conversion.OneWayConverter;
import de.uni_trier.wi2.utils.classFactories.*;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class XEStoNESTConverter implements OneWayConverter<String, ArrayList<NESTSequentialWorkflowObject>> {

    protected final Logger logger;

    protected Model model;
    protected Map<String, ClassFactory> factories;
    protected boolean addGlobals;

    protected XEStoNESTConverter(Model model) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.model = model;
        addBaseAndEventClasses();
        initializeFactories();
    }

    protected abstract void initializeFactories();

    protected void addFactory(String key, ClassFactory factory) {
        factories.put(key, factory);
    }

    private void addBaseAndEventClasses() {
        //event class
        SetClass eventClass = model.getClass(Classnames.EVENT);
        if (eventClass != null) return;
        eventClass = (SetClass) model.getSetSystemClass().createSubclass(Classnames.EVENT);
        //base class
        AggregateClass baseClass = model.getClass(Classnames.BASE);
        if (baseClass == null) {
            baseClass = (AggregateClass) model.getAggregateSystemClass().createSubclass(Classnames.BASE);
            baseClass.addAttribute("key", model.getStringSystemClass());
            baseClass.addAttribute("value", model.getDataSystemClass());
            baseClass.setAbstract(true);
            baseClass.finishEditing();
        }
        //event class
        eventClass.setElementClass(baseClass);
        eventClass.finishEditing();
    }

    public final void configure(boolean addGlobals) {
        this.addGlobals = addGlobals;
    }

    /**
     * Prints the name of all the classes that were created during converting the XES-File or String.
     *
     * @param printKey If True, in Addition to each class name the key for which the class was created gets returned as well.
     */
    public final void printCreatedClasses(Boolean printKey) {
        System.out.println("Event Class: " + Classnames.EVENT + "\n");
        for (ClassFactory factory : factories.values()) {
            for (Map.Entry<String, String> entry : factory.getNamesOfCreatedClasses().entrySet()) {
                StringBuilder str = new StringBuilder();
                if (printKey) str.append(entry.getKey()).append(": ");
                str.append(entry.getValue()).append("\n");
                System.out.println(str);
            }
        }
    }

    /**
     * Returns a list of the names of all the classes that were created during converting the XES-File or String.
     *
     * @param addKey If True, in Addition to each class name the key for which the class was created gets returned as well.
     */
    public final Map<String, List<String>> getCreatedClasses(boolean addKey) {
        Map<String, List<String>> out = new HashMap<>();
        //System.out.println("Event Class: " + EVENT + "\n");

        StringBuilder str;
        List<String> classes;
        for (ClassFactory factory : factories.values()) {
            classes = new ArrayList<>();

            for (Map.Entry<String, String> entry : factory.getNamesOfCreatedClasses().entrySet()) {
                str = new StringBuilder();
                if (addKey) str.append(entry.getKey()).append(": ");
                str.append(entry.getValue());
                classes.add(str.toString());
            }


            if (factory.getClass().equals(CollectionClassFactory.class)) {
                out.put(Classnames.COLLECTION, classes);
            } else if (factory.getClass().equals(ContainerClassFactory.class)) {
                out.put(Classnames.CONTAINER, classes);
            } else if (factory.getClass().equals(ListClassFactory.class)) {
                out.put(Classnames.LIST, classes);
            } else if (factory.getClass().equals(ContinuousClassFactory.class)) {
                out.put(Classnames.CONTINUOUS, classes);
            } else if (factory.getClass().equals(DiscreteClassFactory.class)) {
                out.put(Classnames.DISCRETE, classes);
            } else if (factory.getClass().equals(DurationClassFactory.class)) {
                out.put(Classnames.DURATION, classes);
            } else if (factory.getClass().equals(LiteralClassFactory.class)) {
                out.put(Classnames.LITERAL, classes);
            } else if (factory.getClass().equals(IDClassFactory.class)) {
                out.put(Classnames.ID, classes);
            } else if (factory.getClass().equals(BooleanClassFactory.class)) {
                out.put(Classnames.BOOLEAN, classes);
            } else if (factory.getClass().equals(TimestampClassFactory.class)) {
                out.put(Classnames.TIMESTAMP, classes);
            }
        }

        return out;
    }
}
