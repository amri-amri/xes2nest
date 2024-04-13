package de.uni_trier.wi2.conversion.sax;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.procake.data.object.DataObjectUtils;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.CollectionObject;
import de.uni_trier.wi2.utils.classFactories.ClassFactory;
import org.deckfour.xes.model.impl.XsDateTimeFormat;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Stack;

class GlobalsExtractor extends DefaultHandler {
    DataObjectUtils dataObjectUtils = new DataObjectUtils();


    boolean inGlobal = false;

    Stack<AggregateObject> containerStack = null;
    private Model model;
    private Map<String, ClassFactory> factories;

    private ArrayList<AggregateObject> eventGlobals = null;
    private ArrayList<AggregateObject> traceGlobals = null;


    private String globalScope = null;


    @Override
    public void startDocument() throws SAXException {

        containerStack = new Stack<>();

        eventGlobals = new ArrayList<>();
        traceGlobals = new ArrayList<>();


    }

    @Override
    public void endDocument() throws SAXException {
        containerStack = null;


    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equals("global")) {
            inGlobal = true;
            globalScope = attributes.getValue("scope");
        }
        if (!inGlobal) return;


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

        }

        switch (qName) {
            case "string":
            case "date":
            case "int":
            case "float":
            case "boolean":
            case "id":
                if (containerStack.isEmpty()) {
                    switch (globalScope) {
                        case "trace" -> traceGlobals.add(nObject);
                        case "event" -> eventGlobals.add(nObject);
                    }
                    break;
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

        if (qName.equals("global")) {
            inGlobal = false;
            globalScope = null;
        }
        if (!inGlobal) return;

        switch (qName) {
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
                if (containerStack.isEmpty()) {
                    switch (globalScope) {
                        case "trace" -> traceGlobals.add(nObject);
                        case "event" -> eventGlobals.add(nObject);
                    }
                    break;
                } else {
                    // we are inside container attribute (some level inside of event)
                    AggregateObject container = containerStack.peek();
                    CollectionObject containerContent = (CollectionObject) container.getAttributeValue("value");
                    containerContent.addValue(nObject);
                }
                break;
        }


    }

    public void setFactories(Map<String, ClassFactory> factories) {
        this.factories = factories;
    }


    public void setModel(Model model) {
        this.model = model;
    }


    public ArrayList<AggregateObject> getEventGlobals() {
        return eventGlobals;
    }

    public ArrayList<AggregateObject> getTraceGlobals() {
        return traceGlobals;
    }

}