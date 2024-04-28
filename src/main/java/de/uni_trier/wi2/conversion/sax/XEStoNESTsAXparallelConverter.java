package de.uni_trier.wi2.conversion.sax;

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

public class XEStoNESTsAXparallelConverter extends XEStoNESTsAXConverter {


    private int maxThreads;
    private ArrayList<NESTSequentialWorkflowObject> workflows;
    private ConverterThread[] converters;
    public XEStoNESTsAXparallelConverter(Model model, int maxThreads) {
        super(model);
        this.maxThreads = maxThreads;
        workflows = new ArrayList<>();
    }

    @Override
    public ArrayList<NESTSequentialWorkflowObject> convert(String xes) {
        try {
            StringBuilder logWithoutTraces = new StringBuilder();
            ArrayList<String> traces = new ArrayList<>();

            String[] splitLog = xes.split("<trace");
            logWithoutTraces.append(splitLog[0]);
            converters = new ConverterThread[splitLog.length-1];
            for (int i = 1; i < splitLog.length; i++){
                String[] tracePlus = splitLog[i].split("</trace>");
                logWithoutTraces.append(tracePlus[1]);//todo does it exist?
                String trace = "<trace" + tracePlus[0] + "</trace>";
                ConverterThread thread = new ConverterThread(trace);
                thread.start();
                converters[i-1] = thread;
            }

            for (ConverterThread thr : converters) thr.join();
            return workflows;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private class ConverterThread extends Thread{
        private final String trace;

        ConverterThread(final String trace){
            this.trace = trace;
        }
        @Override
        public void run() {
            XEStoNESTsAXConverter converter = new XEStoNESTsAXConverter(model);
            converter.configure(createSubclasses, includeXMLattributes);
            workflows.addAll(converter.convert(trace));
        }
    }


}
