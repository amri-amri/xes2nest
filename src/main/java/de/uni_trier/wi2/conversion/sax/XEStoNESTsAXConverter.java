package de.uni_trier.wi2.conversion.sax;

import de.uni_trier.wi2.conversion.AbstractXEStoNESTConverter;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.utils.classFactories.*;
import org.apache.commons.io.IOUtils;
import org.apache.xerces.jaxp.DocumentBuilderImpl;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class XEStoNESTsAXConverter extends AbstractXEStoNESTConverter {

    public XEStoNESTsAXConverter(Model model){
        super(model);
    }

    @Override
    protected void initializeFactories() {
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
    public ArrayList<NESTSequentialWorkflowObject> convert(String xes) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            XesHandler xesHandler = new XesHandler();
            xesHandler.setModel(model);
            xesHandler.setFactories(factories);
            if (addGlobals) {
                GlobalsExtractor globalsExtractor = new GlobalsExtractor();
                globalsExtractor.setModel(model);
                globalsExtractor.setFactories(factories);

                saxParser.parse(IOUtils.toInputStream(xes, StandardCharsets.UTF_8), globalsExtractor);

                xesHandler.setTraceGlobals(globalsExtractor.getTraceGlobals());
                xesHandler.setEventGlobals(globalsExtractor.getEventGlobals());
            }

            saxParser.parse(IOUtils.toInputStream(xes, StandardCharsets.UTF_8), xesHandler);
            return xesHandler.getWorkflows();
        } catch (ParserConfigurationException | SAXException | IOException e){
            logger.error("Exception while parsing xes string! Exception: {}", e.getMessage());
            return null;
        }
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









}
