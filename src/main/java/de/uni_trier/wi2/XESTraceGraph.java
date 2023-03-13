package org.example;

import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XTrace;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link org.example.XESGraph} Interface.
 * In this implementation, on creation, event and trace attributes can be provided.
 * They are than added to all events that do not already have matching keys and to the trace attributes respectively.
 * This should be used to make sure that all event and trace attributes have all global attributes provided in the log.
 * Also, the graph has all global trace attributes of the original XES log.
 * The graph does not have any edges by default, but edges can be added manually through different methods.
 */
public class XESTraceGraph implements XESGraph {

    /**
     * Underlying trace of graph.
     */
    private final XTrace trace;

    /**
     * Adjacency map representing the directional edges of the graph.
     * Can be left null if no edges are added or removed through filters.
     */
    private Map<XID, Set<XID>> edges = null;

    /**
     * True if edges by document order should be added, false else.
     * Makes initialization of {@link org.example.XESTraceGraph#edges} unnecessary for internal representation if no further edges are required.
     */
    private boolean defaultEdges = false;

    /**
     * Creates a new XESTraceGraph.
     * In addition to the trace that serves as the base of the graph, eventAttributes and traceAttributes need to be provided.
     * They are than added to all events of the trace and the trace respectively.
     * the attributes parameters should be used to provide the global attributes defined in the log the trace comes from.
     * If the key of a provided attribute already exists in an attribute collection it should be added to, the existing attribute is not replaced.
     *
     * @param trace           trace that serves as base of the graph
     * @param eventAttributes attributes to be added to all events.
     * @param traceAttributes attributes to be added to the trace.
     */
    public XESTraceGraph(XTrace trace, Collection<XAttribute> eventAttributes, Collection<XAttribute> traceAttributes) {
        this.trace = trace;
        addGlobalTraceAttributes(traceAttributes);
        addGlobalEventAttributes(eventAttributes);
    }

    private void addGlobalTraceAttributes(Collection<XAttribute> globalTraceAttributes) {
        for (XAttribute globalAtt : globalTraceAttributes) {
            if (!trace.getAttributes().containsKey(globalAtt.getKey())) {
                trace.getAttributes().put(globalAtt.getKey(), globalAtt);
            }
        }
    }

    private void addGlobalEventAttributes(Collection<XAttribute> globalEventAttributes) {
        for (XAttribute globalAtt : globalEventAttributes) {
            for (XEvent event : trace) {
                if (!event.getAttributes().containsKey(globalAtt.getKey())) {
                    event.getAttributes().put(globalAtt.getKey(), globalAtt);
                }
            }
        }
    }

    /**
     * Connects each event with the one following it through a directed edge.
     */
    public void addEdgesByDocumentOrder() {
        defaultEdges = true;
        if (edges != null) {
            for (int e = 0; e < size() - 1; e++) addEdge(getNodeIdByIndex(e), getNodeIdByIndex(e + 1));
        }
    }

    /**
     * Creates edges between all events that fulfill the criteria defined by the filters.
     *
     * @param f1 Filter that is applied to find all events that should be beginnings of edges.
     * @param f2 Filter that is applied to find all events that should be endings of edges.
     */
    public void addEdges(Filter f1, Filter f2) {
        if (edges == null) {
            edges = createBaseMap();
        }
        Iterator<XEvent> fromIt = trace.stream().filter(f1::filter).iterator();
        List<XEvent> filteredTo = trace.stream().filter(f2::filter).collect(Collectors.toList());
        while (fromIt.hasNext()) {
            XEvent from = fromIt.next();
            for (XEvent event : filteredTo) {
                addEdge(from.getID(), event.getID());
            }
        }
    }

    /**
     * Removes edges between all events that fulfill the criteria defined by the filters.
     *
     * @param f1 Filter that is applied to find all events that are matching beginnings of edges.
     * @param f2 Filter that is applied to find all events that are matching endings of edges.
     */
    public void removeEdges(Filter f1, Filter f2) {
        if (edges == null) edges = createBaseMap();
        Iterator<XEvent> fromIt = trace.stream().filter(f1::filter).iterator();
        List<XEvent> filteredTo = trace.stream().filter(f2::filter).collect(Collectors.toList());
        while (fromIt.hasNext()) {
            XEvent from = fromIt.next();
            for (XEvent event : filteredTo) {
                removeEdge(from.getID(), event.getID());
            }
        }
    }

    /**
     * Creates a map that can be used to initialize {@link org.example.XESTraceGraph#edges}.
     * If {@link org.example.XESTraceGraph#defaultEdges} is true, the map is initialized with the edges implied by {@link org.example.XESTraceGraph#defaultEdges}.
     * If {@link org.example.XESTraceGraph#defaultEdges} is false, the map is empty
     *
     * @return an initialized map.
     */
    private Map<XID, Set<XID>> createBaseMap() {
        Map<XID, Set<XID>> edges = new HashMap<>(size());
        if (defaultEdges) addEdgesByDocumentOrder();
        return edges;
    }

    /**
     * Adds a new edge to {@link org.example.XESTraceGraph#edges}.
     *
     * @param from ID of the event that should be the beginning of the edge.
     * @param to   ID of the event that should be the end of the edge.
     */
    private void addEdge(XID from, XID to) {
        if (edges == null) edges = createBaseMap();
        Set<XID> set = new TreeSet<>();
        Set<XID> existingSet = edges.putIfAbsent(from, set);
        set = (existingSet == null ? set : existingSet);
        set.add(to);
    }

    /**
     * If existing, removes an edge from {@link org.example.XESTraceGraph#edges}.
     * If the edge does not exist, {@link org.example.XESTraceGraph#edges} stays unchanged.
     *
     * @param from ID of the event that is the beginning of the edge.
     * @param to   ID of the event that is the end of the edge.
     */
    private void removeEdge(XID from, XID to) {
        if (edges == null) edges = createBaseMap();
        Set<XID> set = edges.get(from);
        if (set == null) return;
        set.remove(to);
    }

    /**
     * Returns event at the specified position in the trace.
     *
     * @param idx index of the event to return.
     * @return the event at the specified position of the trace.
     */
    private XEvent getNodeByIndex(int idx) {
        return trace.get(idx);
    }

    /**
     * Returns id of the event at the specified position in the trace.
     *
     * @param idx index of the event whose index should be returned.
     * @return the id of the event at the specified position of the trace.
     */
    private XID getNodeIdByIndex(int idx) {
        return getNodeByIndex(idx).getID();
    }

    @Override
    public XAttributeMap getAttributes() {
        return trace.getAttributes();
    }

    @Override
    public void setAttributes(XAttributeMap xAttributeMap) {
        trace.setAttributes(xAttributeMap);

    }

    @Override
    public boolean hasAttributes() {
        return trace.hasAttributes();
    }

    @Override
    public Set<XExtension> getExtensions() {
        return trace.getExtensions();
    }

    @Override
    public Map<XID, Set<XID>> getEdges() {
        if (edges == null) return createBaseMap();
        return edges;
    }

    @Override
    public Collection<XEvent> getNodes() {
        return trace;
    }

    @Override
    public int size() {
        return trace.size();
    }
}
