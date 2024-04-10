package de.uni_trier.wi2.sax;

import de.uni_trier.wi2.classFactories.*;
import de.uni_trier.wi2.namingUtils.Classnames;
import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.procake.data.model.base.SetClass;
import de.uni_trier.wi2.procake.data.object.nest.impl.NESTSequentialWorkflowObjectImpl;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SaxXesToWorkflowConverter {


    private boolean addGlobals;
    private Model model;
    private Map<String, ClassFactory> factories;

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
        CakeInstance.start();
        SaxXesToWorkflowConverter converter = new SaxXesToWorkflowConverter();
        converter.addGlobals(true);
        converter.setModel(ModelFactory.getDefaultModel());
        ArrayList<NESTSequentialWorkflowObjectImpl> a = converter.convert("src/test/resources/example-files/ADT_EXAMPLES.xes");
        System.out.println("");
    }

    public void addGlobals(boolean addGlobals) {
        this.addGlobals = addGlobals;
    }

    //public ArrayList<NESTSequentialWorkflowObjectImpl> convert(String xes) throws ParserConfigurationException, SAXException, IOException {
    //    SAXParserFactory factory = SAXParserFactory.newInstance();
    //    SAXParser saxParser = factory.newSAXParser();
    //    XesHandler xesHandler = new XesHandler();
    //    File file = new File(URI.create(""));
    //    PrintWriter pw = new PrintWriter(file);
    //    pw.write(xes);
    //    pw.close();
    //    saxParser.parse("", xesHandler);
    //    ArrayList<NESTSequentialWorkflowObjectImpl> result = xesHandler.getWorkflows();
    //    return result;
    //}

    public void setModel(Model model) {
        this.model = model;
        addBaseAndEventClass();
        initializeFactories();
    }

    public ArrayList<NESTSequentialWorkflowObjectImpl> convert(String uri) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        XesHandler xesHandler = new XesHandler();
        xesHandler.setModel(model);
        xesHandler.setFactories(factories);
        if (addGlobals) {
            GlobalsExtractor globalsExtractor = new GlobalsExtractor();
            globalsExtractor.setModel(model);
            globalsExtractor.setFactories(factories);

            saxParser.parse(uri, globalsExtractor);

            xesHandler.setTraceGlobals(globalsExtractor.getTraceGlobals());
            xesHandler.setEventGlobals(globalsExtractor.getEventGlobals());
        }

        saxParser.parse(uri, xesHandler);
        return xesHandler.getWorkflows();
    }

    private void addBaseAndEventClass() {
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

    private void initializeFactories() {
        factories = new HashMap<>();
        addFactory("string", new LiteralClassFactory(model));
        addFactory("date", new TimestampClassFactory(model));
        addFactory("int", new DiscreteClassFactory(model));
        addFactory("float", new ContinuousClassFactory(model));
        addFactory("boolean", new BooleanClassFactory(model));
        addFactory("id", new IDClassFactory(model));
        addFactory("list", new ListClassFactory(model));
        addFactory("container", new ContainerClassFactory(model));

    }

    private void addFactory(String key, ClassFactory factory) {
        factories.put(key, factory);
    }


}
