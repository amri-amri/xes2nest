# XEStoWorkflowConverter Dokumentation
### Vorab:
Um diese Dokumentation und die Funktionsweise des Konverters zu verstehen, sollte man wenigstens etwas mit der Terminologie von XES-Logs und ProCAKE, insbesondere ProCAKE-NEST-Graphen vertraut sein.
## Beschreibung
Die Klasse `XEStoWorkflowConverter` liefert Funktionen zum **Einlesen** von **XES-Dateien** und zur **Konvertierung** dieser zu **`NESTWorkflow`-Graphen**.

Ein Objekt der Klasse `XESToWorkflowConverter` bekommt dabei im Konstruktor ein **ProCAKE Data Model** und einen **Dateipfad** übergeben.

Durch Aufruf von **public NESTWorkflowObject[] getWorkflows()** kann Array konvertierter Workflows ausgegeben werden.

Bei der Konvertierung (und auch schon beim Aufrufen des Konstruktors) werden dem übergebenen Model Klassen hinzugefügt, die auf den Event-Attributen im XES-Trace basieren. Die Liste der erzeugten Klassen kann mit **`public void printCreatedClasses`** in der Konsole ausgegeben werden.

Pro XES-Attributtyp (string, boolean, list, etc.) und XES-Attribut-Key wird dem Model genau eine neue Klasse hinzugefügt.

Möchte man Retrieval mit den Workflows betreiben, sollte man also geeignete Ähnlichkeitsmaße (SimilarityMeasures) konfigurieren.



## Konvertierung
### Event
Ein Event wird zu einem `NESTTaskNodeObject` konvertiert. Alle Attribute werden in einer `XESEventClass` gespeichert, welche als **Semantic Descriptor** des `NESTTaskNodeObject`s dient.

Die Edges werden per `setEdges`-Funktionen gesetzt (siehe [Funktionen](#Funktionen)).
### Trace
Ein Trace wird zu einem `NESTWorkflowObject` konvertiert. Ein Workflow (-Graph) benötigt immer genau einen `NESTWorkflowNodeObject`, in dem Trace-Infos (-Attribute) gespeichert werden.
### Log
Ein Log wird zu einem `NESTWorkflowObject`-Array konvertiert.
## Funktionen
|public & non-static||
|-|-|
|`XEStoWorkflowConverter(final Model model, String filepath)`|Konstruktor. Fügt dem Model die Basisklassen hinzu, auf denen die späteren Realisierungen der Event-Attribute in den `NESTWorkflowTaskNodeObject`'s basieren.|
|`int getSize()`|Gibt die Menge der Traces im übergebenen XES-Log aus.|
|`void addGlobalTraceAttributes()`|Fügt jedem Trace alle globalen Trace-Attribute und deren Standartwert hinzu, sofern das Trace-Attribut noch nicht im Trace enthalten ist.|
|`void addGlobalEventAttributes()`|Fügt jedem Event alle globalen Event-Attribute und deren Standartwert hinzu, sofern das Event-Attribut noch nicht im Event enthalten ist.|
|`void setEdgesByDocumentOrder()`|Im den jeweiligen Workflows werden die Kanten von TaskNode zu TaskNode anhand der Reihenfolge der korrespondierenden Events im Trace gesetzt.|
|`void addEdges(Filter f1, Filter f2)`|In jedem Trace wird von jedem Event `xEvent1`, für das [`f1`](#d)`.filter(xEvent1)==true` ist,  zu jedem Event `xEvent2`, für das [`f2`](#d)`.filter(xEvent2)==true` ist, eine Kante gesetzt.|
|`void removeEdges(Filter f1, Filter f2)`|In jedem Trace wird von jedem Event `xEvent1`, für das [`f1`](#d)`.filter(xEvent1)==true` ist,  zu jedem Event `xEvent2`, für das [`f2`](#d)`.filter(xEvent2)==true` ist, eine Kante gelöscht.|
|`static Filter keyHasStringValueFilter(String key, String value)`|Gibt einen [Filter](#d), der zur Überprüfung von Evxistenz von Event-Attributen und deren Wert dient.|
|`NESTWorkflowObject[] getWorkflows()`|Liefert das Array der konvertierten Workflows.|
|`void printCreatedClasses(Boolean printKey)`|Gibt die Liste der bei der Konvertierung erzeugten Klassen in der Konsole aus.|
## Sonstiges
### `private  interface  Filter{ boolean  filter(XEvent xEvent); }`
Dieses **funktionale Interface** dient zur eigenen Setzung von Kanten in den Workflows.
