# XEStoWorkflowConverter - Documentation
### Beforehand:
To understand this documentation and the way the conversion
works, you should be at least somewhat familiar with the
__XES__ standart and the __ProCAKE__ framework, especially
ProCAKE's `NESTGraphs`.
## Description
The class `FileToXESGraphConverter` provides functionality to
convert **XES files or strings** to `XESTraceGraph`s.

The class `XESTraceGraph` represents **a single XES-Trace**,
to which edges can be added.

The class `XESGraphToWorkflowConverter` provides functionality
to convert a `XESGraph` to a `NESTWorkflow`-Graph.

### File/String-To-Graph-Conversion

Objects of class `FileToXESGraphConverter` are stateless.
Using the **convert**-method, a Collection of `XESTraceGraph`s
is being created.
`XESTraceGraph`s are given the global Event- and Trace-Attributes.

### Manipulation of XES-Graphs

In the implementation `XESTraceGrap`, the nodes of the graph -
the events - cannot be manipulated.
Only directed edges between these nodes can be added or removed
(see [Functions](#functions)).
One way of doing that is to use the order of the traces in the
XES document.
Another way is to provide a `BiFunction<XEvent, XEvent, Boolean>`.
The `BiFunction.apply` method takes two `XEvent`s. The first
one is the one **from** whom a possible edge would be outgoing.
The second one is the one **to** whom the edge would connect to.
The return value is a `Boolean` determining if the edge is
actually added/removed.

### Graph-To-Workflow-Conversion

An object of class `XESGraphToWorkflowConverter` expects a
**ProCAKE DataModel** in its constructor.

In calling **public NESTWorkflowObject.convert(XESGraph origin)**
a converted Workflow can be obtained.

In calling the constructor and in converting, classes based on
XES event attributes are added to the `DataModel`.

The list of created and added classes can be obtained through
`public Map<String, List<String>> getCreatedClasses(boolean addKey)`.

Per XES attribute type (string, boolean, list, etc.) and xes
attribute key, exactly one new class is created and added to
the `DataModel`.

In order to perform retrieval with such converted Workflows,
one should create similarity measures that work on these classes.

## Conversion
### Event
An event is converted to a `NESTTaskNodeObject`.
All attributes are saved in `XESEventClass`-objects, which
serve as **semantic descriptors** of the `NESTTaskNodeObject`.
### Trace
A trace is converted to a `NESTWorkflowObject`.
A workflow (-graph) always requires exactly one
`NESTWorkflowNodeObject`, which contains the trace-attributes.
### Log
A log is converted to an array of `NESTWorkflowObject`.
## Functions
### FileToXESGraphConverter
| public & non-static                           |                                                                                            |
|-----------------------------------------------|--------------------------------------------------------------------------------------------|
| `FileToXESGraphConverter()`                   | constructor                                                                                |
| `Collection<XESGraph> convert(File origin)`   | Converts the given file into a collection of `XESGraph`s, of which each represent a trace. |
| `Collection<XESGraph> convert(String origin)` | Same as above but accepts a String.                                                        |

### XESTraceGraph

| public & non-static                                                                                           |                                                                                                                                                                                                                                                   |
|---------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `XESTraceGraph(XTrace trace, Collection<XAttribute> eventAttributes, Collection<XAttribute> traceAttributes)` | Constructor. The `XTrace` trace serves as basis for the graph. `eventAttributes` are attributes which are to be added to each and every event, where the attribute's key is not present. `traceAttributes` are attributes belonging to the trace. |
| `void addEdgesByDocumentOrder()`                                                                              | Edges are set from node to node (event to event) in the order that these events have in the XES. So for every two events a and b where b is the direct successor of a, there will be an edge (a,b) and no other edges will exist.                 |
| `void addEdges(BiFunction<XEvent, XEvent, Boolean> filter)`                                                   | See [Manipulation of XES-Graphs](#manipulation-of-xes-graphs).                                                                                                                                                                                    |
| `void removeEdges(BiFunction<XEvent, XEvent, Boolean> filter)`                                                | See [Manipulation of XES-Graphs](#manipulation-of-xes-graphs).                                                                                                                                                                                    |
| `Map<XID, Set<XID>> getEdges()`                                                                               | Returns an adjacency map of the edges.                                                                                                                                                                                                            |
| `Collection<XEvent> getNodes()`                                                                               | Returns the events.                                                                                                                                                                                                                               |
| Implementation of `XAttributable`                                                                             | Provides access on the attributes and extension of the trace.                                                                                                                                                                                     |

## XESGraphToWorkflowConverter

| public & non-static                                           |                                                                                   |
|---------------------------------------------------------------|-----------------------------------------------------------------------------------|
| `XESGraphtoWorkflowConverter(final Model model)`              | Constructor. Classes necessary for conversion are added to the model.             |
| `NESTWorkflowObject convert(XESGraph origin)`                 | Converts a `XESGraph` to a `NESTWorkflowObject`.                                  |
| `Map<String, List<String>> getCreatedClasses(boolean addKey)` | Returns a map of created classes (base class name -> list of inheriting classes). |