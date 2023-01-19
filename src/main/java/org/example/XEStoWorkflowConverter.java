package org.example;

import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.base.AggregateClass;
import de.uni_trier.wi2.procake.data.model.base.SetClass;
import de.uni_trier.wi2.procake.data.model.nest.NESTWorkflowClass;
import de.uni_trier.wi2.procake.data.object.DataObjectUtils;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.SetObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTTaskNodeObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowBuilder;
import de.uni_trier.wi2.procake.data.object.nest.utils.NESTWorkflowModifier;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTWorkflowBuilderImpl;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XAttribute;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XElement;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeBooleanImpl;
import org.deckfour.xes.model.impl.XAttributeCollectionImpl;
import org.deckfour.xes.model.impl.XAttributeContainerImpl;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.deckfour.xes.model.impl.XAttributeTimestampImpl;
import org.deckfour.xes.model.impl.XEventImpl;
import org.deckfour.xes.model.impl.XLogImpl;
import org.deckfour.xes.model.impl.XTraceImpl;

public class XEStoWorkflowConverter {

  final private static DataObjectUtils utils = new DataObjectUtils();

  final private Model model;
  final private XLog log;
  final private boolean[][][] edges;


  public XEStoWorkflowConverter(final Model model, String filepath) throws Exception {
    this.model = model;

    XFactoryNaiveImpl xFactory = new XFactoryNaiveImpl();
    XesXmlParser xmlParser = new XesXmlParser(xFactory);
    log = xmlParser.parse(new File(filepath)).get(0);

    edges = new boolean[log.size()][][];
    for (int trace = 0; trace < log.size(); trace++) {
      int numOfEvents = log.get(trace).size();
      edges[trace] = new boolean[numOfEvents][numOfEvents];

      for (int e1 = 0; e1<numOfEvents; e1++) for (int e2 = 0; e2<numOfEvents; e2++) {
        edges[trace][e1][e2] = false;
      }
    }
  }



  //Getter

  public int getSize()
  //returns size of the log, i.e. number of traces
  {
    return log.size();
  }



  //Adding the ProCAKE-Classes to the Model

  private abstract static class Classnames{

    private static final String EVENT = "XESEventClass";


    private static final String BASE = "XESBaseClass";


    private static final String UNNATURALLY_NESTED = "XESUnnaturallyNestedClass";

    private static final String LITERAL = "XESLiteralClass";

    private static final String BOOLEAN = "XESBooleanClass";

    private static final String CONTINUOUS = "XESContinuousClass";

    private static final String DISCRETE = "XESDiscreteClass";

    private static final String TIMESTAMP = "XESTimestampClass";

    private static final String DURATION = "XESDurationClass";

    private static final String ID = "XESIDClass";


    private static final String COLLECTION = "XESCollectionClass";

    private static final String LIST = "XESListClass";

    private static final String CONTAINER = "XESContainerClass";

  }

  public static void addXESClasses(Model model){

    //attribute classes
    AggregateClass baseClass = (AggregateClass) model.getAggregateSystemClass().createSubclass(Classnames.BASE);
    baseClass.addAttribute("key",model.getStringSystemClass());
    baseClass.addAttribute("value",model.getDataSystemClass());
    baseClass.setAbstract(true);
    baseClass.finishEditing();


    //unnaturally nested classes (classes whose objects have key, value and attributes)
    //such object can be nested but it is not their main feature, contrary to collection objects
    //which define their value through nesting
    AggregateClass unnaturallyNested = (AggregateClass) baseClass.createSubclass(Classnames.UNNATURALLY_NESTED);
    unnaturallyNested.addAttribute("attributes",model.getSetSystemClass()); //TODO HIER SOLLTE STATT DER SystemSetClass EINE KLASSE HIN, DIE Sets DEFINIERT, DIE NUR OBJEKTE DER KLASSE XESBaseClass (->Erben) ENTHALTEN
    unnaturallyNested.setAbstract(true);
    unnaturallyNested.finishEditing();

    AggregateClass literalClass = (AggregateClass) unnaturallyNested.createSubclass(Classnames.LITERAL);
    literalClass.updateAttributeType("value",model.getStringSystemClass());
    literalClass.setAbstract(false);
    literalClass.finishEditing();

    AggregateClass booleanClass = (AggregateClass) unnaturallyNested.createSubclass(Classnames.BOOLEAN);
    booleanClass.updateAttributeType("value",model.getBooleanSystemClass());
    booleanClass.setAbstract(false);
    booleanClass.finishEditing();

    AggregateClass continuousClass = (AggregateClass) unnaturallyNested.createSubclass(Classnames.CONTINUOUS);
    continuousClass.updateAttributeType("value",model.getDoubleSystemClass());
    continuousClass.setAbstract(false);
    continuousClass.finishEditing();

    AggregateClass discreteClass = (AggregateClass) unnaturallyNested.createSubclass(Classnames.DISCRETE);
    discreteClass.updateAttributeType("value",model.getIntegerSystemClass());
    discreteClass.setAbstract(false);
    discreteClass.finishEditing();

    AggregateClass timestampClass = (AggregateClass) unnaturallyNested.createSubclass(Classnames.TIMESTAMP);
    timestampClass.updateAttributeType("value",model.getTimestampSystemClass());
    timestampClass.setAbstract(false);
    timestampClass.finishEditing();

    AggregateClass durationClass = (AggregateClass) unnaturallyNested.createSubclass(Classnames.DURATION);
    durationClass.updateAttributeType("value",model.getIntegerSystemClass());
    durationClass.setAbstract(false);
    durationClass.finishEditing();

    AggregateClass idClass = (AggregateClass) unnaturallyNested.createSubclass(Classnames.ID);
    idClass.updateAttributeType("value",model.getStringSystemClass());
    idClass.setAbstract(false);
    idClass.finishEditing();

    //collection classes
    AggregateClass collectionClass = (AggregateClass) baseClass.createSubclass(Classnames.COLLECTION);
    collectionClass.updateAttributeType("value",model.getCollectionSystemClass());
    collectionClass.setAbstract(true);
    collectionClass.finishEditing();

    AggregateClass listClass = (AggregateClass) collectionClass.createSubclass(Classnames.LIST);
    listClass.updateAttributeType("value",model.getListSystemClass()); //TODO Nur XESBaseClass-Objekte als Inhalt (wie oben...)
    listClass.setAbstract(false);
    listClass.finishEditing();

    AggregateClass containerClass = (AggregateClass) collectionClass.createSubclass(Classnames.CONTAINER);
    containerClass.updateAttributeType("value",model.getSetSystemClass()); //TODO Nur XESBaseClass-Objekte als Inhalt (wie oben...)
    containerClass.setAbstract(false);
    containerClass.finishEditing();

    //event class
    SetClass eventClass = (SetClass) model.getSetSystemClass().createSubclass(Classnames.EVENT);
    eventClass.setElementClass(model.getClass(Classnames.BASE));
    eventClass.finishEditing();
  }



  //Adding Global Attributes

  public void addGlobalTraceAttributes(){
    for (XAttribute globalAtt:log.getGlobalTraceAttributes()){
      for (XTrace trace: log){
        if (!trace.getAttributes().containsKey(globalAtt.getKey())) {
          trace.getAttributes().put(globalAtt.getKey(),globalAtt);
        }
      }
    }
  }

  public void addGlobalEventAttributes(){
    for (XAttribute globalAtt:log.getGlobalEventAttributes()){
      for (XTrace trace: log){
        for (XEvent event: trace) {
          if (!event.getAttributes().containsKey(globalAtt.getKey())) {
            event.getAttributes().put(globalAtt.getKey(), globalAtt);
          }
        }
      }
    }
  }



  //Setting Edges

  public void setEdgesByDocumentOrder(){
    for (int t = 0; t<log.size(); t++){
      int numOfEvents = log.get(t).size();
      for (int e = 0; e<numOfEvents-1; e++) edges[t][e][e+1] = true;
    }
  }

  public void addEdges(Filter f1, Filter f2){
    for (int t = 0; t<log.size(); t++){
      XTrace trace = log.get(t);
      int numOfEvents = trace.size();

      for (int e1 = 0; e1<numOfEvents; e1++){
        if ( f1.filter(trace.get(e1)) ) {
          for (int e2 = 0; e2<numOfEvents; e2++) {
            if ( f2.filter(trace.get(e2)) ) {

              edges[t][e1][e2] = true;

            }
          }
        }
      }

    }
  }

  public void removeEdges(Filter f1, Filter f2){
    for (int t = 0; t<log.size(); t++){
      XTrace trace = log.get(t);
      int numOfEvents = trace.size();

      for (int e1 = 0; e1<numOfEvents; e1++){
        if ( f1.filter(trace.get(e1)) ) {
          for (int e2 = 0; e2<numOfEvents; e2++) {
            if ( f2.filter(trace.get(e2)) ) {

              edges[t][e1][e2] = false;

            }
          }
        }
      }

    }
  }

  public void addLifecycleStandardEdges(){
    // state -> transition
    addEdges(keyHasStringValueFilter("lifecycle:state","Ready"),
        keyHasStringValueFilter("lifecycle:transition","assign"));

    addEdges(keyHasStringValueFilter("lifecycle:state","Running"),
        keyHasStringValueFilter("lifecycle:transition","ate_abort"));

    addEdges(keyHasStringValueFilter("lifecycle:state","Start"),
        keyHasStringValueFilter("lifecycle:transition","autoskip"));

    addEdges(keyHasStringValueFilter("lifecycle:state","InProgress"),
        keyHasStringValueFilter("lifecycle:transition","complete"));

    addEdges(keyHasStringValueFilter("lifecycle:state","NotRunning"),
        keyHasStringValueFilter("lifecycle:transition","manualskip"));

    addEdges(keyHasStringValueFilter("lifecycle:state","Open"),
        keyHasStringValueFilter("lifecycle:transition","pi_abort"));

    addEdges(keyHasStringValueFilter("lifecycle:state","Assigned"),
        keyHasStringValueFilter("lifecycle:transition","reassign"));

    addEdges(keyHasStringValueFilter("lifecycle:state","Suspended"),
        keyHasStringValueFilter("lifecycle:transition","resume"));

    addEdges(keyHasStringValueFilter("lifecycle:state","Start"),
        keyHasStringValueFilter("lifecycle:transition","schedule"));

    addEdges(keyHasStringValueFilter("lifecycle:state","Assigned"),
        keyHasStringValueFilter("lifecycle:transition","start"));

    addEdges(keyHasStringValueFilter("lifecycle:state","InProgress"),
        keyHasStringValueFilter("lifecycle:transition","suspend"));

    addEdges(keyHasStringValueFilter("lifecycle:state","NotRunning"),
        keyHasStringValueFilter("lifecycle:transition","withdraw"));

    // transition -> state

    addEdges(keyHasStringValueFilter("lifecycle:transition","assign"),
        keyHasStringValueFilter("lifecycle:state","Assigned"));

    addEdges(keyHasStringValueFilter("lifecycle:transition","ate_abort"),
        keyHasStringValueFilter("lifecycle:state","Aborted"));

    addEdges(keyHasStringValueFilter("lifecycle:transition","autoskip"),
        keyHasStringValueFilter("lifecycle:state","Obsolete"));

    addEdges(keyHasStringValueFilter("lifecycle:transition","complete"),
        keyHasStringValueFilter("lifecycle:state","Completed"));

    addEdges(keyHasStringValueFilter("lifecycle:transition","manualskip"),
        keyHasStringValueFilter("lifecycle:state","Obsolete"));

    addEdges(keyHasStringValueFilter("lifecycle:transition","pi_abort"),
        keyHasStringValueFilter("lifecycle:state","Aborted"));

    addEdges(keyHasStringValueFilter("lifecycle:transition","reassign"),
        keyHasStringValueFilter("lifecycle:state","Assigned"));

    addEdges(keyHasStringValueFilter("lifecycle:transition","resume"),
        keyHasStringValueFilter("lifecycle:state","InProgress"));

    addEdges(keyHasStringValueFilter("lifecycle:transition","schedule"),
        keyHasStringValueFilter("lifecycle:state","Ready"));

    addEdges(keyHasStringValueFilter("lifecycle:transition","start"),
        keyHasStringValueFilter("lifecycle:state","InProgress"));

    addEdges(keyHasStringValueFilter("lifecycle:transition","suspend"),
        keyHasStringValueFilter("lifecycle:state","Suspended"));

    addEdges(keyHasStringValueFilter("lifecycle:transition","withdraw"),
        keyHasStringValueFilter("lifecycle:state","Exited"));

  }

  public static Filter keyHasStringValueFilter(String key, String value){
    return xEvent -> {
      XAttribute attribute = xEvent.getAttributes().get((Object) key);
      if (attribute==null) return false;
      return attribute.toString().equals(value);
    };
  }

  private interface Filter{ boolean filter(XEvent xEvent); }



  //Conversion

  public NESTWorkflowObject[] getWorkflows() throws Exception {
    //Konvertieren der Traces
    NESTWorkflowObject[] workflows = new NESTWorkflowObject[log.size()];
    for (int i = 0; i<workflows.length; i++){
      workflows[i] = getWorkflow(i,"T"+ i);
    }
    return workflows;
  }

  private NESTWorkflowObject getWorkflow(int index, String id) throws Exception {
    XTrace xTrace = log.get(index);
    NESTWorkflowBuilder<NESTWorkflowObject> builder = new NESTWorkflowBuilderImpl();
    NESTWorkflowObject workflow = builder.createNESTWorkflowGraphObject(id, NESTWorkflowClass.CLASS_NAME,null);
    NESTWorkflowModifier traceModifier = workflow.getModifier();

    //put trace attributes in WorkflowNode
    workflow.getWorkflowNode().setSemanticDescriptor(getAttributeSet(xTrace));

    //put TaskNodes in Workflow
    int size = xTrace.size();

    if (size<1) return workflow;

    NESTTaskNodeObject[] events = new NESTTaskNodeObject[size];

    for (int e = 0; e<size; e++){
      events[e] = traceModifier.insertNewTaskNode(getEventSet(xTrace.get(e)));
    }

    for (int e1 = 0; e1<size; e1++) for (int e2 = 0; e2<size; e2++) if (edges[index][e1][e2]){
      traceModifier.insertNewControlflowEdge(events[e1],events[e2],null);
    }

    return workflow;
  }

  private SetObject getEventSet(XEvent event) throws Exception {
    SetObject eventSet = model.createObject(Classnames.EVENT);

    XAttributeMap attributes = event.getAttributes();
    Set attributeKeys = attributes.keySet();

    for (Object key:attributeKeys){
      XAttribute attribute = attributes.get(key);
      eventSet.addValue(convertAttribute(attribute));
    }

    return eventSet;
  }

  private AggregateObject convertAttribute(XAttribute attribute) throws Exception {
    switch(attribute.getClass().getSimpleName()){
      case "XAttributeLiteralImpl":
        XAttributeLiteralImpl XESliteral = (XAttributeLiteralImpl) attribute;
        AggregateObject literal = model.createObject(Classnames.LITERAL);
        literal.setAttributeValue("key",utils.createStringObject(XESliteral.getKey()));
        literal.setAttributeValue("value",utils.createStringObject(XESliteral.getValue()));
        literal.setAttributeValue("attributes",getAttributeSet(XESliteral));
        return literal;
      case "XAttributeBooleanImpl":
        XAttributeBooleanImpl XESboolean = (XAttributeBooleanImpl) attribute;
        AggregateObject bool = model.createObject(Classnames.BOOLEAN);
        bool.setAttributeValue("key",utils.createStringObject(XESboolean.getKey()));
        bool.setAttributeValue("value",utils.createBooleanObject(Boolean.parseBoolean(XESboolean.toString())));
        bool.setAttributeValue("attributes",getAttributeSet(XESboolean));
        return bool;
      case "XAttributeContinuousImpl":
        XAttributeLiteralImpl XEScontinuous = (XAttributeLiteralImpl) attribute;
        AggregateObject continuous = model.createObject(Classnames.CONTINUOUS);
        continuous.setAttributeValue("key",utils.createStringObject(XEScontinuous.getKey()));
        continuous.setAttributeValue("value",utils.createDoubleObject(Double.parseDouble(XEScontinuous.getValue())));
        continuous.setAttributeValue("attributes",getAttributeSet(XEScontinuous));
        return continuous;
      case "XAttributeDiscreteImpl":
        XAttributeLiteralImpl XESdiscrete = (XAttributeLiteralImpl) attribute;
        AggregateObject discrete = model.createObject(Classnames.DISCRETE);
        discrete.setAttributeValue("key",utils.createStringObject(XESdiscrete.getKey()));
        discrete.setAttributeValue("value",utils.createIntegerObject(Integer.parseInt(XESdiscrete.getValue())));
        discrete.setAttributeValue("attributes",getAttributeSet(XESdiscrete));
        return discrete;
      case "XAttributeTimestampImpl":
        XAttributeTimestampImpl XEStimestamp = (XAttributeTimestampImpl) attribute;
        AggregateObject timestamp = model.createObject(Classnames.LITERAL);
        timestamp.setAttributeValue("key",utils.createStringObject(XEStimestamp.getKey()));
        timestamp.setAttributeValue("value",utils.createStringObject(XEStimestamp.getValue().toString())); //TODO: Statt StringObject TimestampObject
        timestamp.setAttributeValue("attributes",getAttributeSet(XEStimestamp));
        return timestamp;
      case "XAttributeDurationImpl":
        XAttributeLiteralImpl XESduration = (XAttributeLiteralImpl) attribute;
        AggregateObject duration = model.createObject(Classnames.DURATION);
        duration.setAttributeValue("key",utils.createStringObject(XESduration.getKey()));
        duration.setAttributeValue("value",utils.createIntegerObject(Integer.parseInt(XESduration.getValue())));
        duration.setAttributeValue("attributes",getAttributeSet(XESduration));
        return duration;
      case "XAttributeIDImpl":
        XAttributeLiteralImpl XESid = (XAttributeLiteralImpl) attribute;
        AggregateObject id = model.createObject(Classnames.ID);
        id.setAttributeValue("key",utils.createStringObject(XESid.getKey()));
        id.setAttributeValue("value",utils.createStringObject(XESid.getValue()));
        id.setAttributeValue("attributes",getAttributeSet(XESid));
        return id;
      case "XAttributeCollectionImpl":
        XAttributeCollectionImpl XEScollection = (XAttributeCollectionImpl) attribute;
        AggregateObject collection = model.createObject(Classnames.COLLECTION);
        collection.setAttributeValue("key",utils.createStringObject(XEScollection.getKey()));
        collection.setAttributeValue("value",getAttributeSet(XEScollection));
        return collection;
      case "XAttributeContainerImpl":
        XAttributeContainerImpl XEScontainer = (XAttributeContainerImpl) attribute;
        AggregateObject container = model.createObject(Classnames.CONTAINER);
        container.setAttributeValue("key",utils.createStringObject(XEScontainer.getKey()));
        container.setAttributeValue("value",getAttributeSet(XEScontainer));
        return container;
      case "XAttributeListImpl":
        XAttributeCollectionImpl XESlist = (XAttributeCollectionImpl) attribute;
        AggregateObject list = model.createObject(Classnames.LIST);
        list.setAttributeValue("key",utils.createStringObject(XESlist.getKey()));
        list.setAttributeValue("value",getAttributeSet(XESlist));
        return list;
      default:
        throw new Exception("Invalid class");
    }
  }

  private SetObject getAttributeSet(Object o) throws Exception {
    if (! (o instanceof XElement || o instanceof XAttribute || o instanceof XTrace)) throw new Exception("The object of class "+o.getClass().getSimpleName()+" is neither an XElement nor an XAttribute");

    SetObject attributeSet = utils.createSetObject();

    XAttributeMap attributes = (XAttributeMap) o.getClass().getMethod("getAttributes").invoke(o);
    Set attributeKeys = attributes.keySet();
    for (Object key:attributeKeys){
      XAttribute attribute = attributes.get(key);
      attributeSet.addValue(convertAttribute(attribute));
    }
    return attributeSet;
  }



  //print

  public void print() throws Exception {
    printXESObject(log);
  }

  private static final String ANSI_RESET = "\u001B[0m";
  private static final String ANSI_RED = "\u001B[31m";
  private static final String ANSI_GREEN = "\u001B[32m";
  private static final String ANSI_YELLOW = "\u001B[33m";

  private static void printXESObject(Object o) throws Exception {
    switch(o.getClass().getSimpleName()){

      case "XLogImpl":
        System.out.println("L");
        XLogImpl log = (XLogImpl) o;

        System.out.print(ANSI_GREEN);
        System.out.println("Extensions:");
        System.out.print(ANSI_YELLOW);
        for (XExtension extension:log.getExtensions()){
          printXESObject(extension);
        }
        System.out.print(ANSI_GREEN);
        System.out.println("Global Trace Attributes:");
        System.out.print(ANSI_YELLOW);
        for (XAttribute traceGlobal:log.getGlobalTraceAttributes()){
          printXESObject(traceGlobal);
        }
        System.out.print(ANSI_GREEN);
        System.out.println("Global Event Attributes:");
        System.out.print(ANSI_YELLOW);
        for (XAttribute eventGlobal:log.getGlobalEventAttributes()){
          printXESObject(eventGlobal);
        }
        System.out.print(ANSI_GREEN);
        System.out.println("Event Classifiers:");
        System.out.print(ANSI_YELLOW);
        for (XEventClassifier classifier:log.getClassifiers()){
          printXESObject(classifier);
        }
        System.out.print(ANSI_GREEN);
        System.out.println("Log Attributes:");
        System.out.print(ANSI_YELLOW);
        printXAttributes(log);

        System.out.print(ANSI_RESET);


        for (XTrace trace:log){
          System.out.println();
          printXESObject(trace);
        }
        break;
      case "XTraceImpl":
        System.out.println("T");
        XTraceImpl trace = (XTraceImpl) o;

        printXAttributes(trace);

        for (int i = 0; i<trace.size(); i++){
          System.out.println();
          printXESObject(trace.get(i));
        }
        break;
      case "XEventImpl":
        System.out.println("E");
        XEventImpl event = (XEventImpl) o;
        printXAttributes(event);
        break;
      case "XAttributeLiteralImpl":
      case "XAttributeBooleanImpl":
      case "XAttributeContinuousImpl":
      case "XAttributeDiscreteImpl":
      case "XAttributeTimestampImpl":
      case "XAttributeDurationImpl":
      case "XAttributeIDImpl":
        printNaturallyUnnested((XAttribute) o);
        break;
      case "XAttributeContainerImpl":
      case "XAttributeCollectionImpl":
      case "XAttributeListImpl":
        printNaturallyNested((XAttribute) o);
        break;
      default:
        if (o instanceof XExtension) {
          printXExtension((XExtension) o);
          return;
        }
        throw new Exception("Class " + o.getClass().getSimpleName() + " not recognized");
    }
  }

  private static void printXAttributes(Object o) throws Exception {
    if (! (o instanceof XElement || o instanceof XAttribute)) throw new Exception("The object of class "+o.getClass().getSimpleName()+" is neither an XElement nor an XAttribute");

    XAttributeMap attributes = (XAttributeMap) o.getClass().getMethod("getAttributes").invoke(o);
    Set attributeKeys = attributes.keySet();
    for (Object key:attributeKeys){
      XAttribute attribute = attributes.get(key);
      printXESObject(attribute);
    }
  }

  private static void printNaturallyUnnested(XAttribute o) throws Exception {
    String className = new StringBuilder(new StringBuilder(o.getClass().getSimpleName().substring(10)).reverse().substring(4)).reverse().toString();
    Method getVal = o.getClass().getMethod("getValue");
    System.out.println(className + " > " + o.getKey() + " > " + getVal.invoke(o));
    printXAttributes(o);
  }

  private static void printNaturallyNested(XAttribute o) throws Exception {
    String className = new StringBuilder(new StringBuilder(o.getClass().getSimpleName().substring(10)).reverse().substring(4)).reverse().toString();
    Method getCol = o.getClass().getMethod("getCollection");
    System.out.println(className + " > " + o.getKey() + " >");

    Collection<XAttribute> listCollection = (Collection<XAttribute>) getCol.invoke(o);
    for (XAttribute colEntry : listCollection){
      printXESObject(colEntry);
    }
  }

  private static void printXExtension(XExtension o) {
    System.out.println(o.getName());
    //TODO more information
  }

  private static void printXClassifier(XEventClassifier o){
    System.out.println(o.name());
    //TODO more information
  }
}