package de.uni_trier.wi2.conversion;

import de.uni_trier.wi2.naming.Classnames;
import de.uni_trier.wi2.naming.XESorAggregateAttributeNames;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.procake.data.model.base.ListClass;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.utils.conversion.OneWayConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class XEStoNESTConverter implements OneWayConverter<String, List<NESTSequentialWorkflowObject>> {

    private static boolean initialized = false;

    protected final Logger logger;

    protected Model model;
    protected boolean completeTraces;
    protected boolean includeXMLattributes;
    protected String[] ids;

    /**
     * determines if subclasses are to be created for every (type,@key) combination
     * that occurs in the conversion
     * (type = string, list, float etc., @key = 'key' attribute of the xes tag)
     * for example instead of every string tag being converted to XESStringClass,
     * every tag with key "xyz" creates a subclass with name "XESXyzStringClass",
     * like Eric Brake implemented it before
     */
    protected boolean createSubclasses;
    protected String classifierName;

    protected XEStoNESTConverter(Model model) {
        logger = LoggerFactory.getLogger(this.getClass());
        this.model = model;
        this.ids = null;
        if (!initialized) initializeBaseClasses();
    }

    public void initializeBaseClasses() {

        // XESAttributeClass
        AggregateClass xmlAttributeClass = model.getClass(Classnames.getXMLClassName(Classnames.ATTRIBUTE_CLASS));
        if (xmlAttributeClass == null) {
            xmlAttributeClass = (AggregateClass) model.getAggregateSystemClass().createSubclass(Classnames.getXMLClassName(Classnames.ATTRIBUTE_CLASS));
            xmlAttributeClass.addAttribute(XESorAggregateAttributeNames.NAME, model.getStringSystemClass());
            xmlAttributeClass.addAttribute(XESorAggregateAttributeNames.VALUE, model.getStringSystemClass());
            xmlAttributeClass.setAbstract(false);
            xmlAttributeClass.finishEditing();
        }

        // XESAttributeListClass
        ListClass xmlAttributeListClass = model.getClass(Classnames.getXMLClassName(Classnames.ATTRIBUTE_LIST_CLASS));
        if (xmlAttributeListClass == null) {
            xmlAttributeListClass = (ListClass) model.getListSystemClass().createSubclass(Classnames.getXMLClassName(Classnames.ATTRIBUTE_LIST_CLASS));
            xmlAttributeListClass.setElementClass(xmlAttributeClass);
            xmlAttributeListClass.finishEditing();
        }

        // XESBaseClass
        AggregateClass baseClass = model.getClass(Classnames.getXESClassName(Classnames.ATTRIBUTE_CLASS));
        if (baseClass == null) {
            baseClass = (AggregateClass) model.getAggregateSystemClass().createSubclass(Classnames.getXESClassName(Classnames.ATTRIBUTE_CLASS));
            baseClass.addAttribute(XESorAggregateAttributeNames.KEY, model.getStringSystemClass());
            baseClass.addAttribute(XESorAggregateAttributeNames.ATTRIBUTES, xmlAttributeListClass);
            baseClass.setAbstract(true);
            baseClass.finishEditing();
        }

        // XESBaseListClass
        ListClass baseListClass = model.getClass(Classnames.getXESClassName(Classnames.ATTRIBUTE_LIST_CLASS));
        if (baseListClass == null) {
            baseListClass = (ListClass) model.getListSystemClass().createSubclass(Classnames.getXESClassName(Classnames.ATTRIBUTE_LIST_CLASS));
            baseListClass.setElementClass(baseClass);
            baseListClass.finishEditing();
        }

        // XESAtomicClass
        AggregateClass atomicClass = model.getClass(Classnames.getXESClassName(Classnames.ATOMIC_CLASS));
        if (atomicClass == null) {
            atomicClass = (AggregateClass) baseClass.createSubclass(Classnames.getXESClassName(Classnames.ATOMIC_CLASS));
            atomicClass.addAttribute(XESorAggregateAttributeNames.CHILDREN, baseListClass);
            atomicClass.setAbstract(true);
            atomicClass.finishEditing();
        }

        // XESStringClass
        AggregateClass stringClass = model.getClass(Classnames.getXESClassName(Classnames.STRING_CLASS));
        if (stringClass == null) {
            stringClass = (AggregateClass) atomicClass.createSubclass(Classnames.getXESClassName(Classnames.STRING_CLASS));
            stringClass.addAttribute(XESorAggregateAttributeNames.VALUE, model.getStringSystemClass());
            stringClass.setAbstract(false);
            stringClass.finishEditing();
        }

        // XESDateTimeClass
        AggregateClass dateTimeClass = model.getClass(Classnames.getXESClassName(Classnames.DATETIME_CLASS));
        if (dateTimeClass == null) {
            dateTimeClass = (AggregateClass) atomicClass.createSubclass(Classnames.getXESClassName(Classnames.DATETIME_CLASS));
            dateTimeClass.addAttribute(XESorAggregateAttributeNames.VALUE, model.getTimestampSystemClass());
            dateTimeClass.setAbstract(false);
            dateTimeClass.finishEditing();
        }

        // XESIntegerClass
        AggregateClass integerClass = model.getClass(Classnames.getXESClassName(Classnames.INTEGER_CLASS));
        if (integerClass == null) {
            integerClass = (AggregateClass) atomicClass.createSubclass(Classnames.getXESClassName(Classnames.INTEGER_CLASS));
            integerClass.addAttribute(XESorAggregateAttributeNames.VALUE, model.getIntegerSystemClass());
            integerClass.setAbstract(false);
            integerClass.finishEditing();
        }

        // XESFloatClass
        AggregateClass floatClass = model.getClass(Classnames.getXESClassName(Classnames.FLOAT_CLASS));
        if (floatClass == null) {
            floatClass = (AggregateClass) atomicClass.createSubclass(Classnames.getXESClassName(Classnames.FLOAT_CLASS));
            floatClass.addAttribute(XESorAggregateAttributeNames.VALUE, model.getDoubleSystemClass());
            floatClass.setAbstract(false);
            floatClass.finishEditing();
        }

        // XESBooleanClass
        AggregateClass booleanClass = model.getClass(Classnames.getXESClassName(Classnames.BOOLEAN_CLASS));
        if (booleanClass == null) {
            booleanClass = (AggregateClass) atomicClass.createSubclass(Classnames.getXESClassName(Classnames.BOOLEAN_CLASS));
            booleanClass.addAttribute(XESorAggregateAttributeNames.VALUE, model.getBooleanSystemClass());
            booleanClass.setAbstract(false);
            booleanClass.finishEditing();
        }

        // XESIDClass
        AggregateClass idClass = model.getClass(Classnames.getXESClassName(Classnames.ID_CLASS));
        if (idClass == null) {
            idClass = (AggregateClass) atomicClass.createSubclass(Classnames.getXESClassName(Classnames.ID_CLASS));
            idClass.addAttribute(XESorAggregateAttributeNames.VALUE, model.getStringSystemClass());
            idClass.setAbstract(false);
            idClass.finishEditing();
        }

        // XESListClass
        AggregateClass listClass = model.getClass(Classnames.getXESClassName(Classnames.LIST_CLASS));
        if (listClass == null) {
            listClass = (AggregateClass) baseClass.createSubclass(Classnames.getXESClassName(Classnames.LIST_CLASS));
            listClass.addAttribute(XESorAggregateAttributeNames.VALUE, baseListClass);
            listClass.setAbstract(false);
            listClass.finishEditing();
        }

        // XESEventClass
        ListClass eventClass = model.getClass(Classnames.getXESClassName(Classnames.EVENT_CLASS));
        if (eventClass == null) {
            eventClass = (ListClass) model.getListSystemClass().createSubclass(Classnames.getXESClassName(Classnames.EVENT_CLASS));
            eventClass.setElementClass(baseClass);
            eventClass.finishEditing();
        }

        initialized = true;
    }

    public final void configure(boolean createSubclasses, boolean includeXMLattributes) {
        configure(createSubclasses, includeXMLattributes, null, null);
    }

    public final void configure(boolean createSubclasses, boolean includeXMLattributes, String classifierName, String[] ids) {
        this.createSubclasses = createSubclasses;
        this.completeTraces = classifierName != null;
        this.classifierName = classifierName;
        this.ids = ids;
    }

}
