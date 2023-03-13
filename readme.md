# XEStoWorkflowConverter Dokumentation
### Vorab:
Um diese Dokumentation und die Funktionsweise des Konverters zu verstehen, sollte man wenigstens etwas mit der Terminologie von XES-Logs und ProCAKE, insbesondere ProCAKE-NEST-Graphen vertraut sein.
## Beschreibung
Die Klasse `FileToXESGraphConverter` bietet Funktionalität um **XES-Dateien** in `XESTraceGraphen` zu **konvertieren**.

Die Klasse `XESTraceGraph` repräsentiert einen einzelnen **XES-Trace**, zu dem noch Kanten hinzugefügt werden können.

Die Klasse `XESGraphToWorkflowConverter` bietet Funktionalität zur **Konvertierung** eines `XESGraphen` zu **`NESTWorkflow`-Graphen**.

### Datei-Zu-Graph-Konvertierung

Objekte der Klasse `FileToXESGraphConverter` sind zustandslos.
Mithilfe der **convert**-Methode wird eine Kollektion von `XESTraceGraphen` erzeugt.
Dabei werden den `XESTraceGrapen` die globalen Event- und Trace-Attribute des Logs übergeben.

Sollte das Parsen der XES-Datei fehlschlagen, wird eine entsprechende Exception geworfen.

### Veränderung von XES-Graphen

In der Implementierung `XESTraceGrap` können die Knoten des Graphen, die Events, nicht verändert werden.
Es können nur Kanten entfernt und hinzugefügt werden (siehe [Funktionen](#funktionen)).
Dazu gibt es einerseits die Möglichkeit, Events in der Reihenfolge zu verbinden, wie sie im Log enthalten waren.
Andererseits können Kanten aufgrund bestimmter Eigenschaften der Events hinzugefügt werden, welche durch Implementierungen der `Filter`-Schnittstelle überprüft werden können.

### Graph-Zu-Workflow-Konvertierung

Ein Objekt der Klasse `XESGraphToWorkflowConverter` bekommt im Konstruktor ein **ProCAKE Data Model** übergeben.

Durch den Aufruf von **public NESTWorkflowObject convert(XESGraph origin)** kann ein konvertierter Workflows ausgegeben werden.

Bei der Konvertierung (und auch schon beim Aufrufen des Konstruktors) werden dem übergebenen Model Klassen hinzugefügt, die auf den Event-Attributen im XES-Trace basieren.
Die Liste der erzeugten Klassen kann mit **`public void printCreatedClasses`** in der Konsole ausgegeben werden.
Dabei werden alle Klassen ausgegeben, die jemals von dem Converter-Objekt erzeugt wurden.

Pro XES-Attributtyp (string, boolean, list, etc.) und XES-Attribut-Key wird dem Model genau eine neue Klasse hinzugefügt.

Möchte man Retrieval mit den Workflows betreiben, sollte man also geeignete Ähnlichkeitsmaße (SimilarityMeasures) konfigurieren.



## Konvertierung
### Event
Ein Event wird zu einem `NESTTaskNodeObject` konvertiert. Alle Attribute werden in einer `XESEventClass` gespeichert, welche als **Semantic Descriptor** des `NESTTaskNodeObject`s dient.
### Trace
Ein Trace wird zu einem `NESTWorkflowObject` konvertiert. Ein Workflow (-Graph) benötigt immer genau einen `NESTWorkflowNodeObject`, in dem Trace-Infos (-Attribute) gespeichert werden.
### Log
Ein Log wird zu einem `NESTWorkflowObject`-Array konvertiert.
## Funktionen

### FileToXESGraphConverter
| public & non-static                               |                                                                                                  |
|---------------------------------------------------|--------------------------------------------------------------------------------------------------|
| `FileToXESGraphConverter()`                       | Konstruktor                                                                                      |
| `Collection<XESGraph> convert(File origin)`       | Konvertiert die gegebene Datei in mehrere XESGraphen, von denen jeder einen Trace repräsentiert. |

### XESTraceGraph

| public & non-static                                                                                           |                                                                                                                                                                                                                                                            |
|---------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `XESTraceGraph(XTrace trace, Collection<XAttribute> eventAttributes, Collection<XAttribute> traceAttributes)` | Konstruktor, der übergebene `trace` dient als Grundlage des Graphen, die `eventAttributes` werden zu jedem Event hinzugefügt, in dem der Key des Attributs nicht vorkommt. Die `traceAttributes` werden verwendet, um die Attribute des Trace zu ergänzen. |
| `void addEdgesByDocumentOrder()`                                                                              | Im Graphen werden Kanten von Knoten zu Knoten, also Event zu Event anhand der Reihenfolge der Events im Trace gesetzt.                                                                                                                                     |
| `void addEdges(Filter f1, Filter f2)`                                                                         | Es wird von jedem Event `xEvent1`, für das [`f1`](#sonstiges)`.filter(xEvent1)==true` ist,  zu jedem Event `xEvent2`, für das [`f2`](#sonstiges)`.filter(xEvent2)==true` ist, eine Kante gesetzt.                                                          |
| `void removeEdges(Filter f1, Filter f2)`                                                                      | Es wird von jedem Event `xEvent1`, für das [`f1`](#sonstiges)`.filter(xEvent1)==true` ist,  zu jedem Event `xEvent2`, für das [`f2`](#sonstiges)`.filter(xEvent2)==true` ist, eine Kante gelöscht, falls diese existiert.                                  |
| `Map<XID, Set<XID>> getEdges()`                                                                               | Gibt eine Adjazenz-Map der Kanten im Graphen zurück.                                                                                                                                                                                                       |
| `Collection<XEvent> getNodes()`                                                                               | Gibt die Events zurück, die als Knoten des Graphen dienen.                                                                                                                                                                                                 |
| Implementierung der Schnittstelle `XAttributable`                                                             | Erlaubt Zugriff auf die Attribute und Extensions des Traces.                                                                                                                                                                                               |

## XESGraphToWorkflowConverter

| public & non-static                              |                                                                                                               |
|--------------------------------------------------|---------------------------------------------------------------------------------------------------------------|
| `XESGraphtoWorkflowConverter(final Model model)` | Konstruktor. Klassen, die für die Konvertierung benötigt werden, werden zu dem übergebenen Model hinzugefügt. |
| `NESTWorkflowObject convert(XESGraph origin)`    | Wandelt einen übergebenen Graphen in ein NetWorkflowObject um.                                                |
| `void printCreatedClasses(Boolean printKey)`     | Gibt eine Liste der bei Konvertierungen erzeugten Klassen in der Konsole aus.                                 |

## Sonstiges
### `interface  Filter{ boolean  filter(XEvent xEvent); }`
Dieses **funktionale Interface** dient zur eigenen Setzung von Kanten in den Workflows.
