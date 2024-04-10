package de.uni_trier.wi2.sax;

import de.uni_trier.wi2.classFactories.ClassFactory;
import de.uni_trier.wi2.namingUtils.Classnames;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.procake.data.model.nest.NESTSequentialWorkflowClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.DataObjectUtils;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.object.base.SetObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.impl.NESTSequentialWorkflowObjectImpl;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTAbstractWorkflowModifier;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowBuilder;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTWorkflowBuilderImpl;
import org.deckfour.xes.model.impl.XsDateTimeFormat;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class XesHandler extends DefaultHandler {
    DataObjectUtils dataObjectUtils = new DataObjectUtils();
    Stack<AggregateObject> containerStack = null;
    ArrayList<NESTSequentialWorkflowObjectImpl> workflowList = null;
    NESTWorkflowBuilder<NESTSequentialWorkflowObject> builder = null;
    NESTSequentialWorkflowObject workflow = null;
    NESTAbstractWorkflowModifier workflowModifier = null;
    NESTTaskNodeObject previousTaskNode = null;
    SetObject eventSet = null;
    private Model model;
    private Map<String, ClassFactory> factories;
    private boolean inGlobal = false;
    private ArrayList<AggregateObject> eventGlobals = null;
    private ArrayList<AggregateObject> traceGlobals = null;

    public void setFactories(Map<String, ClassFactory> factories) {
        this.factories = factories;
    }

    public ArrayList<NESTSequentialWorkflowObjectImpl> getWorkflows() {
        return workflowList;
    }

    public void setTraceGlobals(ArrayList<AggregateObject> traceGlobals) {
        this.traceGlobals = traceGlobals;
    }

    public void setEventGlobals(ArrayList<AggregateObject> eventGlobals) {
        this.eventGlobals = eventGlobals;
    }

    @Override
    public void startDocument() throws SAXException {

        containerStack = new Stack<>();

        if (eventGlobals == null) eventGlobals = new ArrayList<>();
        if (traceGlobals == null) traceGlobals = new ArrayList<>();


        builder = new NESTWorkflowBuilderImpl<>();
    }

    @Override
    public void endDocument() throws SAXException {
        containerStack = null;

        eventGlobals = null;
        traceGlobals = null;
        builder = null;

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        System.out.printf("qName: %s%n", qName);
        if (attributes.getLength() > 0) {
            System.out.printf("\tAttributes:%n");
        }
        for (int i = 0; i < attributes.getLength(); i++) {
            System.out.printf("\tKey: %s | Value: %s%n", attributes.getQName(i), attributes.getValue(i));
        }


        if (inGlobal) return;


        ClassFactory factory;
        AggregateClass nClass = null;
        AggregateObject nObject = null;
        String value = null;

        switch (qName) {
            case "string", "date", "int", "float", "boolean", "id", "list", "container" -> {
                factory = factories.get(qName);
                String key = attributes.getValue("key");
                value = attributes.getValue("value");
                nClass = factory.getClass(key);
                nObject = model.createObject(nClass.getName());
                nObject.setAttributeValue("key", dataObjectUtils.createStringObject(key));
            }
        }

        switch (qName) {
            case "log":
                workflowList = new ArrayList<>();
                break;
            case "extension":
                //TODO
                break;
            case "global":
                inGlobal = true;
                break;
            case "classifier":
                //TODO
                break;
            case "trace":

                workflow = builder.createNESTWorkflowGraphObject(null, NESTSequentialWorkflowClass.CLASS_NAME, null);
                workflowModifier = workflow.getModifier();
                workflow.getWorkflowNode().setSemanticDescriptor(dataObjectUtils.createSetObject());

                break;
            case "event":

                eventSet = model.createObject(Classnames.EVENT);

                break;
            case "string":
            case "id":
                nObject.setAttributeValue("value", dataObjectUtils.createStringObject(value));
                break;
            case "date":
                try {
                    nObject.setAttributeValue("value", dataObjectUtils.createTimestampObject(new Timestamp(new XsDateTimeFormat().parseObject(value).getTime())));
                } catch (ParseException e) {
                    fatalError(new SAXParseException(e.getMessage(), null));
                }
                break;
            case "int":
                nObject.setAttributeValue("value", dataObjectUtils.createIntegerObject(Integer.parseInt(value)));
                break;
            case "float":
                nObject.setAttributeValue("value", dataObjectUtils.createDoubleObject(Double.parseDouble(value)));
                break;
            case "boolean":
                nObject.setAttributeValue("value", dataObjectUtils.createBooleanObject(Boolean.parseBoolean(value)));
                break;
            case "list":
                nObject.setAttributeValue("value", dataObjectUtils.createListObject());
                containerStack.push(nObject);
                break;
            case "container":
                nObject.setAttributeValue("value", dataObjectUtils.createSetObject());
                containerStack.push(nObject);
                break;
            default:
                fatalError(new SAXParseException(
                        String.format("Unknown element: qName=%s", qName), null));
                break;

        }

        switch (qName) {
            case "string":
            case "date":
            case "int":
            case "float":
            case "boolean":
            case "id":
                if (workflow == null) {
                    // we are inside log
                    //todo
                } else if (eventSet == null) {
                    // we are inside trace
                    ((SetObject) workflow.getWorkflowNode().getSemanticDescriptor()).addValue(nObject);
                } else if (containerStack.isEmpty()) {
                    // we are inside event
                    eventSet.addValue(nObject);
                } else {
                    // we are inside container attribute (some level inside of event)
                    AggregateObject container = containerStack.peek();
                    CollectionObject containerContent = (CollectionObject) container.getAttributeValue("value");
                    containerContent.addValue(nObject);
                }

        }


    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        System.out.printf("Ended element %s%n", qName);

        switch (qName) {
            case "log":
                //TODO
                break;
            case "extension":
                //TODO
                break;
            case "global":
                inGlobal = false;
                return;
            case "classifier":
                //TODO
                break;
            case "trace":


                SetObject workflowNodeSet = (SetObject) workflow.getWorkflowNode().getSemanticDescriptor();
                List<String> traceAttributeClassNames = workflowNodeSet.getCollection().stream().map(dataObject -> dataObject.getDataClass().getName()).toList();
                for (DataObject traceGlobal : traceGlobals) {
                    if (!traceAttributeClassNames.contains(traceGlobal.getDataClass().getName())) {
                        workflowNodeSet.addValue(traceGlobal);
                    }
                }


                NESTSequentialWorkflowObjectImpl sequentialWorkflow = (NESTSequentialWorkflowObjectImpl) ModelFactory.getDefaultModel().getNESTSequentialWorkflowClass().newObject();
                sequentialWorkflow.transformNESTGraphToNESTSequentialWorkflow(workflow);
                workflowList.add(sequentialWorkflow);

                workflow = null;
                workflowModifier = null;

                previousTaskNode = null;

                break;
            case "event":


                List<String> eventAttributeClassNames = eventSet.getCollection().stream().map(dataObject -> dataObject.getDataClass().getName()).toList();
                for (DataObject eventGlobal : eventGlobals) {
                    if (!eventAttributeClassNames.contains(eventGlobal.getDataClass().getName())) {
                        eventSet.addValue(eventGlobal);
                    }
                }


                NESTTaskNodeObject currentTaskNode = workflowModifier.insertNewTaskNode(eventSet);
                if (previousTaskNode != null) {
                    workflowModifier.insertNewControlflowEdge(previousTaskNode, currentTaskNode, null);
                }
                previousTaskNode = currentTaskNode;

                eventSet = null;

                break;
            case "string":
            case "date":
            case "int":
            case "float":
            case "boolean":
            case "id":
                break;
            case "list":
            case "container":
                AggregateObject nObject = containerStack.pop();
                if (workflow == null) {
                    // we are inside log
                    //todo
                } else if (eventSet == null) {
                    // we are inside trace
                    ((SetObject) workflow.getWorkflowNode().getSemanticDescriptor()).addValue(nObject);
                } else if (containerStack.isEmpty()) {
                    // we are inside event
                    eventSet.addValue(nObject);
                } else {
                    // we are inside container attribute (some level inside of event)
                    AggregateObject container = containerStack.peek();
                    CollectionObject containerContent = (CollectionObject) container.getAttributeValue("value");
                    containerContent.addValue(nObject);
                }
                break;
        }


    }

    public void setModel(Model model) {
        this.model = model;
    }


}