package de.uni_trier.wi2.conversion.sax;

import de.uni_trier.wi2.conversion.XEStoNESTConverter;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class XEStoNESTsAXConverter extends XEStoNESTConverter {

    public XEStoNESTsAXConverter(Model model) {
        super(model);
    }


    @Override
    public ArrayList<NESTSequentialWorkflowObject> convert(String xes) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            XESHandler xesHandler = new XESHandler();
            xesHandler.setModel(model);
            xesHandler.configure(createSubclasses, includeXMLattributes, classifierName);
            saxParser.parse(IOUtils.toInputStream(xes, StandardCharsets.UTF_8), xesHandler);
            return xesHandler.getWorkflows();
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("Exception while parsing xes string! Exception: {}", e.getMessage());
            return null;
        }
    }
}
