package de.uni_trier.wi2.conversion.sax;

import de.uni_trier.wi2.naming.Classnames;
import de.uni_trier.wi2.naming.KeyNameConverter;
import de.uni_trier.wi2.naming.XESTagNames;
import de.uni_trier.wi2.naming.XESorAggregateAttributeNames;
import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.procake.data.model.nest.NESTSequentialWorkflowClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.DataObjectUtils;
import de.uni_trier.wi2.procake.data.object.base.*;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequenceNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTAbstractWorkflowModifier;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowBuilder;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTWorkflowBuilderImpl;
import org.deckfour.xes.model.impl.XsDateTimeFormat;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;

import static de.uni_trier.wi2.conversion.sax.XESHandler.ExceptionHandler.*;

public final class XESHandler extends DefaultHandler {

    private final DataObjectUtils dataObjectUtils = new DataObjectUtils();
    private boolean inTrace;
    private boolean inEvent;
    private boolean inGlobal;
    private Scope globalScope;
    private HashSet<Classifier> classifiers;
    //private ArrayList<AggregateObject> globalEventAttributes;
    private ArrayList<AggregateObject> globalTraceAttributes;
    private ListObject event;
    private ArrayList<ListObject> logEvents;
    private Model model;
    private Stack<AggregateObject> listStack;
    ArrayList<NESTSequentialWorkflowObject> workflows = new ArrayList<>();
    private NESTSequentialWorkflowObject workflow;
    private boolean completeTraces = false;
    private boolean createSubclasses = false;
    private boolean includeXMLattributes = false;
    private String classifierName = null;
    private NESTTaskNodeObject previousTaskNode;
    private NESTWorkflowBuilder<NESTSequentialWorkflowObject> builder;
    private NESTAbstractWorkflowModifier workflowModifier;
    String[] ids;
    private int idIndex;

    public void configure(boolean createSubclasses, boolean includeXMLattributes, String classifierName, String[] ids) {
        this.createSubclasses = createSubclasses;
        this.includeXMLattributes = includeXMLattributes;
        this.completeTraces = classifierName != null;
        this.classifierName = classifierName;
        this.ids = ids;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    @Override
    public void startDocument() {
        inTrace = false;
        inEvent = false;
        inGlobal = false;
        globalScope = null;
        classifiers = new HashSet<>();
        logEvents = new ArrayList<>();
        listStack = new Stack<>();
        //globalEventAttributes = new ArrayList<>();
        globalTraceAttributes = new ArrayList<>();
        previousTaskNode = null;
        event = null;
        //workflows = new ArrayList<>();
        workflow = null;
        builder = new NESTWorkflowBuilderImpl<>();
        idIndex = 0;
    }

    @Override
    public void endDocument() throws SAXParseException {

        // Are traces to be completed and are there even potential completing events?
        if (completeTraces && !logEvents.isEmpty()) completeTraces();

        if (ids!=null) while (idIndex < ids.length && idIndex < workflows.size()){
            workflows.get(idIndex).setId(ids[idIndex]);
            idIndex++;
        }

        ids = null;
        idIndex = -1;
        globalScope = null;
        listStack = null;
        builder = null;
        workflowModifier = null;
        previousTaskNode = null;
        classifiers = null;
        logEvents = null;
    }

    public ArrayList<NESTSequentialWorkflowObject> getWorkflows() {
        return workflows;

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXParseException {
        if (qName.equals(XESTagNames.TRACE)) inTrace = true;

        // phase 1
        if (!inTrace && completeTraces) phase1_StartElement(qName, attributes);

        // phase 2
        if (inTrace) phase2_StartElement(qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
        // phase 1
        if (!inTrace && completeTraces) phase1_EndElement(qName);

        // phase 2
        if (inTrace) phase2_EndElement(qName);
    }

    private void completeTraces() throws SAXParseException {

        // Is the classifier with given name *classifierName* even defined properly in the XES?
        int scopes = 0;
        Classifier classifier = null;
        for (Classifier c : classifiers)
            if (c.name().equals(classifierName)) {
                classifier = c;
                scopes++;
                if (scopes == 2) break;
            }
        if (scopes < 2) {
            classifiers = null;
            logEvents = null;
            return;
        }
        if (classifier == null) return;

        // Go through every non-trace event and see if it belongs to a trace
        String[] keys = classifier.keys();
        Map<String, NESTSequentialWorkflowObject> newWorkflows = new HashMap<>();
        for (ListObject logEvent : logEvents) {
            // Get identity according to classifier
            StringBuilder eventIdentity = new StringBuilder();
            int keysUsed = 0;
            for (String key : keys) {
                for (DataObject attr : logEvent.getValues()) {
                    AggregateObject aggr = (AggregateObject) attr;
                    if (((StringObject) aggr.getAttributeValue(XESorAggregateAttributeNames.KEY)).getNativeString().equals(key)) {
                        eventIdentity.append(aggr.getAttributeValue(XESorAggregateAttributeNames.VALUE).toString());
                        keysUsed++;
                        if (keysUsed == keys.length) break;
                    }
                }
            }
            if (keysUsed < keys.length)
                throw new SAXParseException("Non-trace-event defined with too few global attributes.", null);

            // Find traces with same identity and append event to it
            boolean traceFound = false;
            for (NESTSequentialWorkflowObject workflow : workflows) {
                StringBuilder traceIdentity = new StringBuilder();
                keysUsed = 0;
                for (String key : keys) {
                    for (DataObject attr : ((ListObject) workflow.getWorkflowNode().getSemanticDescriptor()).getValues()) {
                        AggregateObject aggr = (AggregateObject) attr;
                        if (((StringObject) aggr.getAttributeValue(XESorAggregateAttributeNames.KEY)).getNativeString().equals(key)) {
                            traceIdentity.append(aggr.getAttributeValue(XESorAggregateAttributeNames.VALUE).toString());
                            keysUsed++;
                            if (keysUsed == keys.length) break;
                        }
                    }
                }
                if (keysUsed < keys.length)
                    throw new SAXParseException("Trace defined with too few global attributes.", null);
                if (eventIdentity.toString().contentEquals(traceIdentity)) {
                    NESTTaskNodeObject prev = null;
                    if (!workflow.getEndTaskNodes().isEmpty())
                        prev = (NESTTaskNodeObject) workflow.getEndTaskNodes().toArray(NESTSequenceNodeObject[]::new)[0];
                    NESTTaskNodeObject next = workflow.getModifier().insertNewTaskNode(logEvent);
                    if (prev != null) workflow.getModifier().insertNewControlflowEdge(prev, next, null);
                    traceFound = true;
                }
            }
            if (!traceFound) {
                String[] keys_ = classifier.keys();
                NESTSequentialWorkflowObject newTrace = newWorkflows.computeIfAbsent(eventIdentity.toString(), k -> {
                    NESTSequentialWorkflowObject newWorkflow = (new NESTWorkflowBuilderImpl<NESTSequentialWorkflowObject>()).createEmptyNESTWorkflowObject(null, NESTSequentialWorkflowClass.CLASS_NAME);
                    newWorkflow.getModifier().insertNewWorkflowNode(dataObjectUtils.createListObject());
                    for (AggregateObject globalTraceAttr : globalTraceAttributes) {
                        AggregateObject traceAttr = (AggregateObject) globalTraceAttr.copy();
                        String traceKey = ((StringObject) traceAttr.getAttributeValue(XESorAggregateAttributeNames.KEY)).getNativeString();
                        if (Arrays.asList(keys_).contains(traceKey)) {
                            DataObject logEventAttrVal = null;
                            for (DataObject eventAttr : logEvent.getValues()) {
                                AggregateObject eventAggr = (AggregateObject) eventAttr;
                                if (((StringObject) eventAggr.getAttributeValue(XESorAggregateAttributeNames.KEY)).getNativeString().equals(traceKey)) {
                                    logEventAttrVal = eventAggr.getAttributeValue(XESorAggregateAttributeNames.VALUE);
                                    break;
                                }
                            }
                            traceAttr.setAttributeValue(XESorAggregateAttributeNames.VALUE, logEventAttrVal);
                        }
                        ((ListObject) newWorkflow.getWorkflowNode().getSemanticDescriptor()).addValue(traceAttr);
                    }
                    return newWorkflow;
                });
                NESTTaskNodeObject prev = null;
                if (!newTrace.getEndTaskNodes().isEmpty())
                    prev = (NESTTaskNodeObject) newTrace.getEndTaskNodes().toArray(NESTSequenceNodeObject[]::new)[0];
                NESTTaskNodeObject next = newTrace.getModifier().insertNewTaskNode(logEvent);
                if (prev != null) newTrace.getModifier().insertNewControlflowEdge(prev, next, null);
            }
        }

        workflows.addAll(newWorkflows.values());
    }

    private void phase1_StartElement(String qName, Attributes attributes) throws SAXParseException {
        if (inTrace) return;

        String scope;

        switch (qName) {
            case XESTagNames.TRACE:
                inTrace = true;
                return;
            case XESTagNames.EVENT:
                event = model.createObject(Classnames.getXESClassName(Classnames.EVENT));
                inEvent = true;
                return;
            case XESTagNames.GLOBAL:
                inGlobal = true;
                scope = attributes.getValue(XESorAggregateAttributeNames.SCOPE);
                throwGlobalException(scope);
                scope = scope.trim();
                if (scope.equalsIgnoreCase(XESTagNames.EVENT)) globalScope = Scope.EVENT;
                else if (scope.equalsIgnoreCase(XESTagNames.TRACE)) globalScope = Scope.TRACE;
                return;
            case XESTagNames.CLASSIFIER:
                String name = attributes.getValue(XESorAggregateAttributeNames.NAME);
                scope = attributes.getValue(XESorAggregateAttributeNames.SCOPE);
                String keys = attributes.getValue(XESorAggregateAttributeNames.KEYS);
                throwClassifierException(name, scope, keys);

                String[] keyArray = keys.split(" ");

                if (scope == null) {
                    classifiers.add(new Classifier(name, Scope.EVENT, keyArray));
                    classifiers.add(new Classifier(name, Scope.TRACE, keyArray));
                    return;
                }
                scope = scope.trim();
                if (scope.equalsIgnoreCase(XESTagNames.EVENT))
                    classifiers.add(new Classifier(name, Scope.EVENT, keyArray));
                else if (scope.equalsIgnoreCase(XESTagNames.TRACE))
                    classifiers.add(new Classifier(name, Scope.TRACE, keyArray));
                return;
            case XESTagNames.STRING:
            case XESTagNames.DATE:
            case XESTagNames.INT:
            case XESTagNames.FLOAT:
            case XESTagNames.BOOLEAN:
            case XESTagNames.ID:
            case XESTagNames.LIST:
                break;
            default:
                return;
        }

        createXESBaseClassObjects_StartElement(qName, attributes);

    }

    private void phase1_EndElement(String qName) {
        if (qName.equals(XESTagNames.TRACE)) {
            inTrace = false;
            return;
        }
        if (inTrace) return;

        switch (qName) {
            case XESTagNames.LOG, XESTagNames.EXTENSION:
                return;
            case XESTagNames.EVENT:
                logEvents.add(event);
                event = null;
                inEvent = false;
                return;
            case XESTagNames.GLOBAL:
                globalScope = null;
                inGlobal = false;
            case XESTagNames.CLASSIFIER:
                return;
            case XESTagNames.STRING:
            case XESTagNames.DATE:
            case XESTagNames.INT:
            case XESTagNames.FLOAT:
            case XESTagNames.BOOLEAN:
            case XESTagNames.ID:
            case XESTagNames.LIST:
                AggregateObject object = listStack.pop();
                if (!listStack.isEmpty()) {
                    AggregateObject parent = listStack.peek();
                    DataClass parentClass = parent.getDataClass();
                    ListObject children;

                    if (parentClass.isSubclassOf(model.getClass(Classnames.getXESClassName(Classnames.ATOMIC)))) {
                        children = (ListObject) parent.getAttributeValue(XESorAggregateAttributeNames.CHILDREN);
                    } else {
                        children = (ListObject) parent.getAttributeValue(XESorAggregateAttributeNames.VALUE);
                    }
                    children.addValue(object);
                } else if (inGlobal) {
                    //if (globalScope.equals(Scope.EVENT)) globalEventAttributes.add(object);
                    if (globalScope.equals(Scope.TRACE)) globalTraceAttributes.add(object);
                } else if (inEvent) {
                    event.addValue(object);
                }
        }
    }

    private void phase2_StartElement(String qName, Attributes attributes) throws SAXParseException {
        switch (qName) {
            case XESTagNames.TRACE -> {
                workflow = builder.createEmptyNESTWorkflowObject(null, NESTSequentialWorkflowClass.CLASS_NAME);
                workflowModifier = workflow.getModifier();
                workflowModifier.insertNewWorkflowNode(dataObjectUtils.createListObject());
                previousTaskNode = null;
            }
            case XESTagNames.EVENT -> {
                event = model.createObject(Classnames.getXESClassName(Classnames.EVENT));
                inEvent = true;
            }
            case XESTagNames.STRING, XESTagNames.DATE, XESTagNames.INT, XESTagNames.FLOAT, XESTagNames.BOOLEAN, XESTagNames.ID, XESTagNames.LIST ->
                    createXESBaseClassObjects_StartElement(qName, attributes);
        }
    }

    private void phase2_EndElement(String qName) {
        switch (qName) {
            case XESTagNames.LOG -> {
            }
            case XESTagNames.TRACE -> {
                workflows.add(workflow);
                workflow = null;
                workflowModifier = null;
                inTrace = false;
            }
            case XESTagNames.EVENT -> {
                NESTTaskNodeObject currentTaskNode = workflowModifier.insertNewTaskNode(event);
                if (previousTaskNode != null) {
                    workflowModifier.insertNewControlflowEdge(previousTaskNode, currentTaskNode, null);
                }
                previousTaskNode = currentTaskNode;
                event = null;
                inEvent = false;
            }
            case XESTagNames.STRING, XESTagNames.DATE, XESTagNames.INT, XESTagNames.FLOAT, XESTagNames.BOOLEAN, XESTagNames.ID, XESTagNames.LIST -> {
                AggregateObject object = listStack.pop();
                if (!listStack.isEmpty()) {
                    //stack
                    AggregateObject parent = listStack.peek();
                    DataClass parentClass = parent.getDataClass();
                    ListObject children;

                    if (parentClass.isSubclassOf(model.getClass(Classnames.getXESClassName(Classnames.ATOMIC)))) {
                        children = (ListObject) parent.getAttributeValue(XESorAggregateAttributeNames.CHILDREN);
                    } else {
                        children = (ListObject) parent.getAttributeValue(XESorAggregateAttributeNames.VALUE);
                    }
                    children.addValue(object);
                } else if (inEvent) {
                    event.addValue(object);
                } else {
                    ((ListObject) workflow.getWorkflowNode().getSemanticDescriptor()).addValue(object);
                }
            }
        }
    }

    /**
     * Starts creation of an object whose DataClass extends 'XESBaseClass' and pushes it on the {@link #listStack},
     * based on the given XES element with given qName and attributes.
     * The completion of creation takes place (in another method) after potential child elements have been resolved and
     * appended to the object.
     * If {@link #createSubclasses} is set to 'true', a new class for every key-datatype-combination is created
     * and used for the object to be returned.
     *
     * @param qName      name of XES element
     * @param attributes attributes of XES element
     * @throws SAXParseException if there was a problem with the 'key' or 'value' attribute of the XES element
     */
    private void createXESBaseClassObjects_StartElement(String qName, Attributes attributes) throws SAXParseException {
        AggregateObject object;
        ListObject attributeList;
        String key, value;

        key = attributes.getValue(XESorAggregateAttributeNames.KEY);
        value = attributes.getValue(XESorAggregateAttributeNames.VALUE);
        DataObject valueObject;

        switch (qName) {
            case XESTagNames.STRING -> {
                valueObject = getStringValueObjectOrThrowException(key, value);
                object = createSubclassObject(key, Classnames.STRING);
                object.setAttributeValue(XESorAggregateAttributeNames.CHILDREN, model.createObject(Classnames.getXESClassName(Classnames.ATTRIBUTELIST)));
            }
            case XESTagNames.DATE -> {
                valueObject = getDateValueObjectOrThrowException(key, value);
                object = createSubclassObject(key, Classnames.DATETIME);
                object.setAttributeValue(XESorAggregateAttributeNames.CHILDREN, model.createObject(Classnames.getXESClassName(Classnames.ATTRIBUTELIST)));
            }
            case XESTagNames.INT -> {
                valueObject = getIntObjectOrThrowException(key, value);
                object = createSubclassObject(key, Classnames.INTEGER);
                object.setAttributeValue(XESorAggregateAttributeNames.CHILDREN, model.createObject(Classnames.getXESClassName(Classnames.ATTRIBUTELIST)));
            }
            case XESTagNames.FLOAT -> {
                valueObject = getFloatValueObjectOrThrowException(key, value);
                object = createSubclassObject(key, Classnames.FLOAT);
                object.setAttributeValue(XESorAggregateAttributeNames.CHILDREN, model.createObject(Classnames.getXESClassName(Classnames.ATTRIBUTELIST)));
            }
            case XESTagNames.BOOLEAN -> {
                valueObject = getBooleanValueObjectOrThrowException(key, value);
                object = createSubclassObject(key, Classnames.BOOLEAN);
                object.setAttributeValue(XESorAggregateAttributeNames.CHILDREN, model.createObject(Classnames.getXESClassName(Classnames.ATTRIBUTELIST)));
            }
            case XESTagNames.ID -> {
                valueObject = getIDValueObjectOrThrowException(key, value);
                object = createSubclassObject(key, Classnames.ID);
                object.setAttributeValue(XESorAggregateAttributeNames.CHILDREN, model.createObject(Classnames.getXESClassName(Classnames.ATTRIBUTELIST)));
            }
            case XESTagNames.LIST -> {
                valueObject = getListValueObjectOrThrowException(model, key);
                object = createSubclassObject(key, Classnames.LIST);
            }
            default -> {
                return;
            }
        }

        object.setAttributeValue(XESorAggregateAttributeNames.KEY, dataObjectUtils.createStringObject(key));
        object.setAttributeValue(XESorAggregateAttributeNames.VALUE, valueObject);

        if (includeXMLattributes) {
            attributeList = model.createObject(Classnames.getXMLClassName(Classnames.ATTRIBUTELIST));
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getQName(i).equals(XESorAggregateAttributeNames.KEY) || attributes.getQName(i).equals(XESorAggregateAttributeNames.VALUE))
                    continue;
                AggregateObject attributeObject = model.createObject(Classnames.getXMLClassName(Classnames.ATTRIBUTE));
                attributeObject.setAttributeValue(XESorAggregateAttributeNames.NAME, dataObjectUtils.createStringObject(attributes.getQName(i)));
                attributeObject.setAttributeValue(XESorAggregateAttributeNames.VALUE, dataObjectUtils.createStringObject(attributes.getValue(i)));
                attributeList.addValue(attributeObject);
            }
            object.setAttributeValue(XESorAggregateAttributeNames.ATTRIBUTES, attributeList);
        }

        listStack.push(object);
    }

    /**
     * Creates an 'empty' object either of a class with name 'XES'+classNameSuffix or of a subclass of that
     * called 'XES'+{@link KeyNameConverter#getValidName}(key)+classnameSuffix, based on the given XES key attribute
     * and classname suffix.
     * 'Empty' (s.a.) means that none of the AggregateObjects attribute values are set.
     *
     * @param key             key attribute of XES element
     * @param classnameSuffix desired classname suffix and thus superclass
     * @return 'empty' aggregate object of desired (sub-)class
     */
    private AggregateObject createSubclassObject(String key, String classnameSuffix) {
        String baseclassName = Classnames.getXESClassName(classnameSuffix);
        if (!createSubclasses) return model.createObject(baseclassName);
        String className = Classnames.getXESSubClassName(key, classnameSuffix);
        AggregateClass subclass = model.getClass(className);
        if (subclass == null) {
            subclass = (AggregateClass) model.getClass(baseclassName).createSubclass(className);
            subclass.setAbstract(false);
            subclass.finishEditing();
        }
        return model.createObject(className);
    }

    static final class ExceptionHandler {
        static final DataObjectUtils dataObjectUtils = new DataObjectUtils();

        private ExceptionHandler() {
        }

        static void throwClassifierException(String name, String scope, String keys) throws SAXParseException {
            if (name == null || name.isEmpty())
                throw new SAXParseException("Classifier defined without 'name' attribute.", null);
            if (keys == null || keys.isEmpty())
                throw new SAXParseException("Classifier defined without 'keys' attribute.", null);
            if (scope == null) return;
            if (scope.equalsIgnoreCase(XESTagNames.EVENT)) return;
            if (scope.equalsIgnoreCase(XESTagNames.TRACE)) return;
            throw new SAXParseException(String.format("Classifier defined with invalid scope: %s", scope), null);
        }

        static void throwGlobalException(String scope) throws SAXParseException {
            if (scope == null || scope.isEmpty())
                throw new SAXParseException("Global Attribute List defined without scope!", null);
            if (!(scope.trim().equalsIgnoreCase(XESTagNames.TRACE) || scope.trim().equalsIgnoreCase(XESTagNames.EVENT)))
                throw new SAXParseException(String.format("Global Attribute List defined with invalid scope: %s", scope), null);
        }

        static StringObject getStringValueObjectOrThrowException(String key, String value) throws SAXParseException {
            if (key == null || key.isEmpty())
                throw new SAXParseException("String attribute defined without key.", null);
            if (value == null || value.isEmpty())
                throw new SAXParseException("String attribute defined without value.", null);
            return dataObjectUtils.createStringObject(value);
        }

        static TimestampObject getDateValueObjectOrThrowException(String key, String value) throws SAXParseException {
            if (key == null || key.isEmpty()) throw new SAXParseException("Date attribute defined without key.", null);
            if (value == null || value.isEmpty())
                throw new SAXParseException("Date attribute defined without value.", null);
            try {
                return dataObjectUtils.createTimestampObject(new Timestamp(new XsDateTimeFormat().parseObject(value).getTime()));
            } catch (ParseException e) {
                throw new SAXParseException("Date attribute defined with invalid value.", null);
            }
        }

        static IntegerObject getIntObjectOrThrowException(String key, String value) throws SAXParseException {
            if (key == null || key.isEmpty())
                throw new SAXParseException("Integer attribute defined without key.", null);
            if (value == null || value.isEmpty())
                throw new SAXParseException("Integer attribute defined without value.", null);
            try {
                return dataObjectUtils.createIntegerObject(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new SAXParseException("Integer attribute defined with invalid value.", null);
            }
        }

        static DoubleObject getFloatValueObjectOrThrowException(String key, String value) throws SAXParseException {
            if (key == null || key.isEmpty())
                throw new SAXParseException("Integer attribute defined without key.", null);
            if (value == null || value.isEmpty())
                throw new SAXParseException("Integer attribute defined without value.", null);
            double dbl;
            try {
                dbl = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new SAXParseException("Double attribute defined with invalid value.", null);
            }
            //if (dbl == Double.POSITIVE_INFINITY || dbl == Double.NEGATIVE_INFINITY) throw new SAXParseException("Double attribute defined with too large value.", null);
            return dataObjectUtils.createDoubleObject(dbl);
        }

        static BooleanObject getBooleanValueObjectOrThrowException(String key, String value) throws SAXParseException {
            if (key == null || key.isEmpty())
                throw new SAXParseException("Boolean attribute defined without key.", null);
            if (value == null || value.isEmpty())
                throw new SAXParseException("Boolean attribute defined without value.", null);
            try {
                return dataObjectUtils.createBooleanObject(Boolean.parseBoolean(value));
            } catch (NumberFormatException e) {
                throw new SAXParseException("Boolean attribute defined with invalid value.", null);
            }
        }

        static StringObject getIDValueObjectOrThrowException(String key, String value) throws SAXParseException {
            if (key == null || key.isEmpty()) throw new SAXParseException("ID attribute defined without key.", null);
            if (value == null || value.isEmpty())
                throw new SAXParseException("ID attribute defined without value.", null);
            return dataObjectUtils.createStringObject(value);
        }

        static ListObject getListValueObjectOrThrowException(Model model, String key) throws SAXParseException {
            if (key == null || key.isEmpty()) throw new SAXParseException("List attribute defined without key.", null);
            return model.createObject(Classnames.getXESClassName(Classnames.ATTRIBUTELIST));
        }

    }

}
