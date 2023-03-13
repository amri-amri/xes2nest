package de.uni_trier.wi2;

import de.uni_trier.wi2.procake.utils.conversion.OneWayConverter;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Converter that can be used to convert a File that contains XML following XES Standard into {@link de.uni_trier.wi2.XESGraph}.
 * @see <a href=”https://www.xes-standard.org>XES Standard</a>
 * @author Eric Brake
 */
public class FileToXESGraphConverter implements OneWayConverter<File, Collection<XESGraph>> {
    /**
     * Converts a XES-File into a collection of {@link de.uni_trier.wi2.XESGraph}.
     * The conversion turns each trace of the XES-File into a XESGraph.
     * Information about the log, such as Classifiers, are lost.
     * To parse the File, the openXES library is used.
     * @see <a href=”https://www.xes-standard.org/openxes/download#openxes_227”>openXES</a>
     *
     * @param origin object to be converted
     * @return Collection of XESGraphs
     * @throws XESFileToGraphConversionException if parsing fails.
     */
    @Override
    public Collection<XESGraph> convert(File origin) throws XESFileToGraphConversionException {
        XFactoryNaiveImpl xFactory = new XFactoryNaiveImpl();
        XesXmlParser xmlParser = new XesXmlParser(xFactory);
        XLog log;
        Collection<XESGraph> graphs = new ArrayList<>();
        try {
            log = xmlParser.parse(origin).get(0);
        } catch (Exception e) {
            throw new XESFileToGraphConversionException(e.getMessage());
        }
        for (XTrace trace : log) {
            graphs.add(new XESTraceGraph(trace, log.getGlobalEventAttributes(), log.getGlobalTraceAttributes()));
        }
        return graphs;
    }
}
