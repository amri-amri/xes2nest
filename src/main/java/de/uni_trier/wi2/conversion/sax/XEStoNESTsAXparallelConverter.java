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

        final int amountOfGroups = Math.min(maxThreads, splitLog.length-1);
        final StringBuilder[] traceGroups = new StringBuilder[amountOfGroups];

        if (ids != null) {
            final String[][] idGroups = new String[amountOfGroups][];
            final int idGroupSize = (int) Math.ceil((double) ids.length / amountOfGroups);
            for (int i = 0; i < amountOfGroups; i++) {
                traceGroups[i] = new StringBuilder();
                idGroups[i] = new String[idGroupSize];
            }
            int index = 0;
            int idIndex = 0;

            for (int i = 1; i < splitLog.length; i++) {
                String splitter;
                if (splitLog[i].contains("</trace>")) splitter = "</trace>";
                else splitter = "/>";
                String[] tracePlus = splitLog[i].split(splitter);
                //if (tracePlus.length>=2) logWithoutTraces.append(tracePlus[1].trim());
                if (classifierName != null)
                    for (int j = 1; j < tracePlus.length; j++) logWithoutTraces.append(tracePlus[j]);

                String trace = "<trace" + tracePlus[0] + splitter;
                traceGroups[index].append(trace);
                if (i - 1 < ids.length) idGroups[index][idIndex] = ids[i - 1];
                index++;
                if (index == amountOfGroups) {
                    index = 0;
                    idIndex++;
                }
            }


            for (int i = 0; i < amountOfGroups; i++) {
                String traceGroup = "<log>" + traceGroups[i].toString() + "</log>";
                String[] idGroup = idGroups[i];
                executor.submit(() -> {
                    XEStoNESTsAXConverter converter = new XEStoNESTsAXConverter(model);
                    converter.configure(createSubclasses, includeXMLattributes, null, idGroup);
                    List<NESTSequentialWorkflowObject> workflows = converter.convert(traceGroup);
                    syncCollection.addAll(workflows);
                });
            }
        } else {
            for (int i = 0; i < amountOfGroups; i++) {
                traceGroups[i] = new StringBuilder();
            }
            int index = 0;

            for (int i = 1; i < splitLog.length; i++) {
                String splitter;
                if (splitLog[i].contains("</trace>")) splitter = "</trace>";
                else splitter = "/>";
                String[] tracePlus = splitLog[i].split(splitter);
                //if (tracePlus.length>=2) logWithoutTraces.append(tracePlus[1].trim());
                if (classifierName != null)
                    for (int j = 1; j < tracePlus.length; j++) logWithoutTraces.append(tracePlus[j]);

                String trace = "<trace" + tracePlus[0] + splitter;
                traceGroups[index].append(trace);
                index++;
                if (index == amountOfGroups) {
                    index = 0;
                }
            }


            for (int i = 0; i < amountOfGroups; i++) {
                String traceGroup = "<log>" + traceGroups[i].toString() + "</log>";
                executor.submit(() -> {
                    XEStoNESTsAXConverter converter = new XEStoNESTsAXConverter(model);
                    converter.configure(createSubclasses, includeXMLattributes, null, null);
                    List<NESTSequentialWorkflowObject> workflows = converter.convert(traceGroup);
                    syncCollection.addAll(workflows);
                });
            }
        }

        executor.shutdown();
        try {
            if (executor.awaitTermination(24, TimeUnit.HOURS)) {

                if (classifierName !=  null){
                    XESHandler xesHandler = new XESHandler();
                    xesHandler.setModel(model);
                    xesHandler.configure(createSubclasses, includeXMLattributes, classifierName, null);
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
