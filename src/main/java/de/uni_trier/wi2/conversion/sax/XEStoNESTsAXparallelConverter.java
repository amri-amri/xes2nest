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
import java.sql.Array;
import java.util.*;
import java.util.concurrent.*;

public class XEStoNESTsAXparallelConverter extends XEStoNESTsAXConverter {


    private final int maxThreads;
    public XEStoNESTsAXparallelConverter(Model model, int maxThreads) {
        super(model);
        this.maxThreads = maxThreads;
    }

    @Override
    public List<NESTSequentialWorkflowObject> convert(String xes) {
        StringBuilder logWithoutTraces = new StringBuilder();

        String[] splitLog = xes.split("<trace");
        //logWithoutTraces.append(splitLog[0].trim());
        logWithoutTraces.append(splitLog[0]);

        ThreadPoolExecutor executor =
                (ThreadPoolExecutor) Executors.newFixedThreadPool(maxThreads);
        List<NESTSequentialWorkflowObject> syncCollection = Collections.synchronizedList(new ArrayList<>());

        int amountOfGroups = Math.min(maxThreads, splitLog.length-1);
        StringBuilder[] traceGroups = new StringBuilder[amountOfGroups];
        for (int i = 0; i < amountOfGroups; i++) traceGroups[i] = new StringBuilder();
        int index = 0;

        for (int i = 1; i < splitLog.length; i++) {
            //if (splitLog[i].trim().startsWith("/>")) continue;//todo add empty workflow
            if (splitLog[i].startsWith("/>")) continue;//todo add empty workflow
            String[] tracePlus = splitLog[i].split("</trace>");
            if (tracePlus.length > 2) System.out.println("Warning");
            //if (tracePlus.length>=2) logWithoutTraces.append(tracePlus[1].trim());
            if (classifierName!=null && tracePlus.length >= 2) logWithoutTraces.append(tracePlus[1]);

            String trace = "<trace" + tracePlus[0] + "</trace>";
            traceGroups[index].append(trace);
            index++;
            if (index==amountOfGroups) index = 0;
        }

        for (int i = 0; i < amountOfGroups; i++) {
            String traceGroup = "<log>" + traceGroups[i].toString() + "</log>";
            executor.submit(() -> {
                XEStoNESTsAXConverter converter = new XEStoNESTsAXConverter(model);
                converter.configure(createSubclasses, includeXMLattributes);
                List<NESTSequentialWorkflowObject> workflows = converter.convert(traceGroup);
                syncCollection.addAll(workflows);
            });
        }

        executor.shutdown();
        try {
            if (executor.awaitTermination(24L, TimeUnit.HOURS)) {

                if (classifierName !=  null){
                    XESHandler xesHandler = new XESHandler();
                    xesHandler.setModel(model);
                    xesHandler.configure(createSubclasses, includeXMLattributes, classifierName);
                    xesHandler.workflows.addAll(syncCollection);
                    syncCollection.clear();
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();
                    saxParser.parse(IOUtils.toInputStream(logWithoutTraces, StandardCharsets.UTF_8), xesHandler);
                    return xesHandler.getWorkflows();
                }

                return syncCollection;
            }
        } catch (InterruptedException | SAXException | ParserConfigurationException | IOException e) {
            throw new RuntimeException(e);
        }

        throw new RuntimeException("lll");
    }



}
