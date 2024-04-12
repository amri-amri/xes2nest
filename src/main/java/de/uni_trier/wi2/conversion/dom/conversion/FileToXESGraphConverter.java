package de.uni_trier.wi2.conversion.dom.conversion;

import de.uni_trier.wi2.conversion.dom.error.XESFileToGraphConversionException;
import org.apache.commons.io.IOUtils;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Converter that can be used to convert a File that contains XML following XES Standard into {@link XESGraph}.
 *
 * @author Eric Brake
 * @see <a href=”https://www.xes-standard.org>XES Standard</a>
 */
public class FileToXESGraphConverter {
    boolean addGlobals;

    public FileToXESGraphConverter(boolean addGlobals) {
        super();
        this.addGlobals = addGlobals;
    }

    /**
     * Converts a XES-File into a collection of {@link XESGraph}.
     * The conversion turns each trace of the XES-File into a XESGraph.
     * Information about the log, such as Classifiers, are lost.
     * To parse the File, the openXES library is used.
     *
     * @param origin object to be converted
     * @return Collection of XESGraphs
     * @throws XESFileToGraphConversionException if parsing fails.
     * @see <a href=”https://www.xes-standard.org/openxes/download#openxes_227”>openXES</a>
     */
    @Deprecated
    public ArrayList<XESTraceGraph> convert(File origin) throws XESFileToGraphConversionException {
        XFactoryNaiveImpl xFactory = new XFactoryNaiveImpl();
        XesXmlParser xmlParser = new XesXmlParser(xFactory);
        XLog log;
        ArrayList<XESTraceGraph> graphs = new ArrayList<>();
        try {
            log = xmlParser.parse(origin).get(0);
        } catch (Exception e) {
            throw new XESFileToGraphConversionException(e.getMessage());
        }
        for (XTrace trace : log) {
            if (addGlobals)
                graphs.add(new XESTraceGraph(trace, log.getGlobalEventAttributes(), log.getGlobalTraceAttributes()));
            else graphs.add(new XESTraceGraph(trace));
        }
        return graphs;
    }

    /**
     * Converts a String into a collection of {@link XESGraph}.
     * The conversion turns each trace of the inside the String into a XESGraph.
     * Information about the log, such as Classifiers, are lost.
     * To parse the File, the openXES library is used.
     *
     * @param origin object to be converted
     * @return Collection of XESGraphs
     * @throws XESFileToGraphConversionException if parsing fails.
     * @see <a href=”https://www.xes-standard.org/openxes/download#openxes_227”>openXES</a>
     */
    public ArrayList<XESTraceGraph> convert(String origin) throws Exception {
        XFactoryNaiveImpl xFactory = new XFactoryNaiveImpl();
        XesXmlParser xmlParser = new XesXmlParser(xFactory);
        XLog log;
        ArrayList<XESTraceGraph> graphs = new ArrayList<>();
        try {
            log = xmlParser.parse(IOUtils.toInputStream(origin, StandardCharsets.UTF_8)).get(0);
        } catch (Exception e) {
            throw new XESFileToGraphConversionException(e.getMessage());
        }
        for (XTrace trace : log) {
            if (addGlobals)
                graphs.add(new XESTraceGraph(trace, log.getGlobalEventAttributes(), log.getGlobalTraceAttributes()));
            else graphs.add(new XESTraceGraph(trace));
        }
        return graphs;
    }
}
