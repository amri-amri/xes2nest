package de.uni_trier.wi2.conversion.dom.conversion;


import de.uni_trier.wi2.conversion.dom.error.XESGraphToWorkflowConversionException;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.procake.data.model.nest.NESTWorkflowClass;
import de.uni_trier.wi2.procake.data.object.DataObjectUtils;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.procake.data.object.base.ListObject;
import de.uni_trier.wi2.procake.data.object.base.SetObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowBuilder;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowModifier;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTWorkflowBuilderImpl;
import de.uni_trier.wi2.utils.classFactories.ClassFactory;
import de.uni_trier.wi2.utils.namingUtils.Classnames;
import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.impl.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Converter that converts a {@link XESGraph} to a {@link de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject}.
 * In the process of conversion, for each attribute key in the Events of the graph, a matching class is created.
 * The converter is able to print a list of all classes that were ever created by it.
 *
 * @author Eric Brake
 */
public class XESGraphToWorkflowConverter {

    /**
     * Service used to create the values of the attributes of the Events.
     */
    final private DataObjectUtils utils = new DataObjectUtils();
    /**
     * Model to which the new ProCAKE classes are added.
     */
    final private Model model;
    private final Map<String, ClassFactory> factories;


    /**
     * Creates new Converter.
     *
     * @param model model to which classes that are created in the process of conversion.
     */
    public XESGraphToWorkflowConverter(final Model model, Map<String, ClassFactory> factories) {
        this.model = model;
        this.factories = factories;
    }


    /**
     * Converts the origin object type to a target object type.
     *
     * @param origin object to be converted
     * @return the converted object
     */
    public NESTWorkflowObject convert(XESGraph origin) {
        NESTWorkflowBuilder<NESTWorkflowObject> builder = new NESTWorkflowBuilderImpl<>();
        //NESTWorkflowObject workflow = builder.createNESTWorkflowGraphObject("T" + UUID.randomUUID(), NESTWorkflowClass.CLASS_NAME, null);
        NESTWorkflowObject workflow = builder.createNESTWorkflowGraphObject(null, NESTWorkflowClass.CLASS_NAME, null);
        NESTWorkflowModifier traceModifier = workflow.getModifier();

        //put trace attributes in WorkflowNode
        workflow.getWorkflowNode().setSemanticDescriptor(createAttributeSet(origin));

        //put TaskNodes in Workflow
        int size = origin.size();

        if (size < 1) return workflow;

        Map<XID, NESTTaskNodeObject> events = new HashMap<>(size);
        for (XEvent current : origin.getNodes()) {
            events.put(current.getID(), traceModifier.insertNewTaskNode(convertEventObject(current)));
        }
        Map<XID, Set<XID>> edges = origin.getEdges();
        edges.forEach((from, xIds) -> {
            for (XID to : xIds) {
                traceModifier.insertNewControlflowEdge(events.get(from), events.get(to), null);
            }
        });
        return workflow;
    }


    /**
     * Converts a given event into a ProCAKE object.
     * The new event object is based on the class created in AbstractXEStoNESTConverter.addBaseAndEventClass().
     *
     * @param event Object to be converted.
     * @return the converted object.
     */
    private SetObject convertEventObject(XEvent event) {
        SetObject eventSet = model.createObject(Classnames.EVENT);
        addAttributes(eventSet, event.getAttributes());
        return eventSet;
    }

    /**
     * Converts the Attributes given in the AttributeMap and adds them to the given ProCAKE object.
     *
     * @param eventCollection object to which the attributes should be added.
     * @param attributes      attributes that should be added to the ProCAKE object.
     */
    private void addAttributes(CollectionObject eventCollection, XAttributeMap attributes) {
        Set<String> attributeKeys = attributes.keySet();

        for (String key : attributeKeys) {
            XAttribute attribute = attributes.get(key);
            eventCollection.addValue(convertAttribute(attribute));
        }
    }

    /**
     * Converts a XAttribute into a ProCAKE object.
     * To do this, a new ProCAKE class representing the key of the XAttribute is created.
     * For the basic structure of this class, the type of the XAttribute is decisive.
     *
     * @param attribute attribute to be converted.
     * @return the converted attribute.
     */
    private AggregateObject convertAttribute(XAttribute attribute) {
        String attributeClassName = attribute.getClass().getSimpleName();
        ClassFactory factory = factories.get(attributeClassName);
        if (factory == null) {
            throw new XESGraphToWorkflowConversionException("XAttribute class " + attributeClassName + " is unknown.");
        }
        AggregateClass nClass = factory.getClass(attribute.getKey());
        AggregateObject nObject = model.createObject(nClass.getName());
        nObject.setAttributeValue("key", utils.createStringObject(attribute.getKey()));
        switch (attributeClassName) {
            case "XAttributeLiteralImpl" -> {
                XAttributeLiteralImpl XESLiteral = (XAttributeLiteralImpl) attribute;
                nObject.setAttributeValue("value", utils.createStringObject(XESLiteral.getValue()));
                nObject.setAttributeValue("attributes", createAttributeSet(XESLiteral));
                return nObject;
            }
            case "XAttributeBooleanImpl" -> {
                XAttributeBooleanImpl XESBoolean = (XAttributeBooleanImpl) attribute;
                nObject.setAttributeValue("value", utils.createBooleanObject(XESBoolean.getValue()));
                nObject.setAttributeValue("attributes", createAttributeSet(XESBoolean));
                return nObject;
            }
            case "XAttributeContinuousImpl" -> {
                XAttributeContinuousImpl XESContinuous = (XAttributeContinuousImpl) attribute;
                nObject.setAttributeValue("value", utils.createDoubleObject(XESContinuous.getValue()));
                nObject.setAttributeValue("attributes", createAttributeSet(XESContinuous));
                return nObject;
            }
            case "XAttributeDiscreteImpl" -> {
                XAttributeDiscreteImpl XESDiscrete = (XAttributeDiscreteImpl) attribute;
                nObject.setAttributeValue("value", utils.createIntegerObject(((Long.valueOf(XESDiscrete.getValue()).intValue()))));
                nObject.setAttributeValue("attributes", createAttributeSet(XESDiscrete));
                return nObject;
            }
            case "XAttributeTimestampImpl" -> {
                XAttributeTimestampImpl XESTimestamp = (XAttributeTimestampImpl) attribute;
                nObject.setAttributeValue("value", utils.createTimestampObject((new Timestamp(XESTimestamp.getValue().getTime()))));
                nObject.setAttributeValue("attributes", createAttributeSet(XESTimestamp));
                return nObject;
            }
            case "XAttributeIDImpl" -> {
                XAttributeIDImpl XESid = (XAttributeIDImpl) attribute;
                nObject.setAttributeValue("value", utils.createStringObject(XESid.getValue().toString()));
                nObject.setAttributeValue("attributes", createAttributeSet(XESid));
                return nObject;
            }
            case "XAttributeCollectionImpl" -> {
                XAttributeCollectionImpl XESCollection = (XAttributeCollectionImpl) attribute;
                nObject.setAttributeValue("value", createAttributeSet(XESCollection));
                return nObject;
            }
            case "XAttributeContainerImpl" -> {
                XAttributeContainerImpl XESContainer = (XAttributeContainerImpl) attribute;
                nObject.setAttributeValue("value", createAttributeSet(XESContainer));
                return nObject;
            }
            case "XAttributeListImpl" -> {
                XAttributeListImpl XESList = (XAttributeListImpl) attribute;
                nObject.setAttributeValue("value", createAttributeList(XESList));
                return nObject;
            }
            default ->
                    throw new XESGraphToWorkflowConversionException("XAttribute class " + attributeClassName + " is unknown.");
        }
    }

    /**
     * Converts a given XES object that has attributes into a ProCAKE List object.
     *
     * @param o Object to be converted.
     * @return the converted object.
     */
    private ListObject createAttributeList(XAttributable o) {
        ListObject attributeList = utils.createListObject();
        addAttributes(attributeList, o.getAttributes());
        return attributeList;
    }

    /**
     * Converts a given XES object that has attributes into a ProCAKE Set object.
     *
     * @param o Object to be converted.
     * @return the converted object.
     */
    private SetObject createAttributeSet(XAttributable o) {
        SetObject attributeSet = utils.createSetObject();
        addAttributes(attributeSet, o.getAttributes());
        return attributeSet;
    }


}