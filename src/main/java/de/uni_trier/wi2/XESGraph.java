package de.uni_trier.wi2;


import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XAttributable;
import org.deckfour.xes.model.XEvent;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Interface to represent a trace of the XES Standard as a graph.
 * As underlying structure of the graph, elements of the openXES library have to be used.
 * The graph provides the events and directional, unweighted edges between the events that make up the graph.
 * It also provides information that belong the underlying trace.
 * @see <a href=”https://www.xes-standard.org>XES Standard</a>
 * @see <a href=”https://www.xes-standard.org/openxes/download#openxes_227”>openXES</a>
 * @author Eric Brake
 */
public interface XESGraph extends XAttributable {

    /**
     * Returns the edges of the graph represented in a map similar to an adjacency list.
     * Edges are directional going from the event whose {@link org.deckfour.xes.id.XID} is the key to the events contained in the value set.
     * @return adjacency map with {@link org.deckfour.xes.id.XID} as identifiers of the nodes.
     */
    Map<XID, Set<XID>> getEdges();

    /**
     * Returns the nodes of the graph, namely the events of the trace.
     * @return all events of the graph's trace.
     */
    Collection<XEvent> getNodes();

    /**
     * Returns the size of the graph, which resembles the number of nodes.
     * @return number of nodes in the graph.
     */
    int size();
}
