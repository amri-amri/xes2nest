package de.uni_trier.wi2;

import de.uni_trier.wi2.classFactories.*;
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
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.extension.XExtension;
import org.deckfour.xes.factory.XFactoryNaiveImpl;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.*;
import org.deckfour.xes.model.impl.*;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;

@Deprecated
public class XEStoWorkflowConverter {

  final private static DataObjectUtils utils = new DataObjectUtils();

  final private Model model;
  final private XLog log;
  private List<List<Set<Integer>>> edges;

  private boolean defaultEdges = false;

  /**
   * Map that contains the factories used to create custom classes for each type and key combo of XES attributes.
   */
  private Map<String, ClassFactory> factories;


  public XEStoWorkflowConverter(final Model model, String filepath) throws Exception {
    this.model = model;

    XFactoryNaiveImpl xFactory = new XFactoryNaiveImpl();
    XesXmlParser xmlParser = new XesXmlParser(xFactory);
    log = xmlParser.parse(new File(filepath)).get(0);
    edges = null;
    initializeFactories();
    addEventClass();
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


    private static final String NATURALLY_NESTED = "XESNaturallyNestedClass";

    private static final String COLLECTION = "XESCollectionClass";

    private static final String LIST = "XESListClass";

    private static final String CONTAINER = "XESContainerClass";

  }

  private void addEventClass(){
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
    if (defaultEdges) {
      System.out.println("Edges already added!");
      return;
    }
    defaultEdges = true;
    if (edges != null) {
      for (int t = 0; t<log.size(); t++){
        int numOfEvents = log.get(t).size();
        for (int e = 0; e<numOfEvents-1; e++) edges.get(t).get(e).add(e + 1);
        }
      }
  }

  public void addEdges(Filter f1, Filter f2){
    if (edges == null) {
      initEdges();
    }
    for (int t = 0; t<log.size(); t++){
      XTrace trace = log.get(t);
      int numOfEvents = trace.size();

      for (int e1 = 0; e1<numOfEvents; e1++){
        if ( f1.filter(trace.get(e1)) ) {
          for (int e2 = 0; e2<numOfEvents; e2++) {
            if ( f2.filter(trace.get(e2)) ) {
              edges.get(t).get(e1).add(e2);
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
              edges.get(t).get(e1).remove(e2);
            }
          }
        }
      }

    }
  }

  private void initEdges() {
    edges = new ArrayList(log.size());
    for (int trace = 0; trace < log.size(); trace++) {
      int numOfEvents = log.get(trace).size();
      edges.add(trace, new ArrayList(numOfEvents));
      List<Set<Integer>> edgesOfTrace = edges.get(trace);
      for (int i = 0; i<numOfEvents; i++) {
        edgesOfTrace.add(i, new TreeSet<>());
        Set<Integer> current = edgesOfTrace.get(i);
        if (defaultEdges && i < numOfEvents - 1) {
          current.add(i + 1);
        }
      }
    }
  }

  @Deprecated
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
      XAttribute attribute = xEvent.getAttributes().get(key);
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
    if (edges == null) {
      if (defaultEdges) {
        for (int e1 = 0; e1<size - 1; e1++) {
          traceModifier.insertNewControlflowEdge(events[e1],events[e1 + 1],null);
        }
      }
    }
    else {
      for (int e1 = 0; e1<size; e1++) {
        for (int e2: edges.get(index).get(e1)){
          traceModifier.insertNewControlflowEdge(events[e1],events[e2],null);
        }
      }
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
    String attributeClassName = attribute.getClass().getSimpleName();
    ClassFactory factory = factories.get(attributeClassName);
    AggregateClass nClass = factory.getClass(attribute.getKey());
    AggregateObject nObject = model.createObject(nClass.getName());
    nObject.setAttributeValue("key",utils.createStringObject(attribute.getKey()));
    switch(attributeClassName){
      case "XAttributeLiteralImpl":
        XAttributeLiteralImpl XESliteral = (XAttributeLiteralImpl) attribute;
        nObject.setAttributeValue("value",utils.createStringObject(XESliteral.getValue()));
        nObject.setAttributeValue("attributes",getAttributeSet(XESliteral));
        return nObject;
      case "XAttributeBooleanImpl":
        XAttributeBooleanImpl XESboolean = (XAttributeBooleanImpl) attribute;
        nObject.setAttributeValue("value",utils.createBooleanObject(XESboolean.getValue()));
        nObject.setAttributeValue("attributes",getAttributeSet(XESboolean));
        return nObject;
      case "XAttributeContinuousImpl":
        XAttributeContinuousImpl XEScontinuous = (XAttributeContinuousImpl) attribute;
        nObject.setAttributeValue("value",utils.createDoubleObject(XEScontinuous.getValue()));
        nObject.setAttributeValue("attributes",getAttributeSet(XEScontinuous));
        return nObject;
      case "XAttributeDiscreteImpl":
        XAttributeDiscreteImpl XESdiscrete = (XAttributeDiscreteImpl) attribute;
        nObject.setAttributeValue("value",utils.createIntegerObject(((Long.valueOf(XESdiscrete.getValue()).intValue()))));
        nObject.setAttributeValue("attributes",getAttributeSet(XESdiscrete));
        return nObject;
      case "XAttributeTimestampImpl":
        XAttributeTimestampImpl XEStimestamp = (XAttributeTimestampImpl) attribute;
        nObject.setAttributeValue("value",utils.createTimestampObject((new Timestamp(XEStimestamp.getValue().getTime()))));
        nObject.setAttributeValue("attributes",getAttributeSet(XEStimestamp));
        return nObject;
      case "XAttributeIDImpl":
        XAttributeIDImpl XESid = (XAttributeIDImpl) attribute;
        nObject.setAttributeValue("value",utils.createStringObject(XESid.getValue().toString()));
        nObject.setAttributeValue("attributes",getAttributeSet(XESid));
        return nObject;
      case "XAttributeCollectionImpl":
        XAttributeCollectionImpl XEScollection = (XAttributeCollectionImpl) attribute;
        nObject.setAttributeValue("value",getAttributeSet(XEScollection));
        return nObject;
      case "XAttributeContainerImpl":
        XAttributeContainerImpl XEScontainer = (XAttributeContainerImpl) attribute;
        nObject.setAttributeValue("value",getAttributeSet(XEScontainer));
        return nObject;
      case "XAttributeListImpl":
        XAttributeListImpl XESlist = (XAttributeListImpl) attribute;
        nObject.setAttributeValue("value",getAttributeSet(XESlist));
        return nObject;
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

  @Deprecated //This function was only used for the development the actual functionality of the converter
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
        if (o instanceof XEventClassifier) {
          printXClassifier((XEventClassifier) o);
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


  /**
   * Prints the name of all the classes that were created during converting the XES-File.
   * @param printKey If True, in Addition to each class name the key for which the class was created gets returned as well.
   */
  public  void printCreatedClasses(Boolean printKey) {
      for (ClassFactory factory: factories.values()) {
        for (Map.Entry<String, String> entry: factory.getNamesOfCreatedClasses().entrySet()) {
          StringBuilder str = new StringBuilder();
          if (printKey) str.append(entry.getKey()).append(": ");
          str.append(entry.getValue()).append("\n");
          System.out.println(str);
        }
      }
  }

  /**
   * Adds new factory to the Factory map. Overwrites factory in map if Key already exists.
   * @param key Should be the classname of the XES attribute type implementation for which the Factory should be used.
   * @param factory Factory for creating classes of a certain XES type.
   */
  private void addFactory(String key, ClassFactory factory) {
    factories.put(key, factory);
  }

  /**
   * Initializes the factories-map and adds the Factory Classes of {@link de.uni_trier.wi2.classFactories} with the mating class names of the {@link org.deckfour.xes.model.impl} implementations as keys.
   */
  private void initializeFactories() {
    factories = new HashMap<>();
    addFactory("XAttributeLiteralImpl", new LiteralClassFactory(model));
    addFactory("XAttributeBooleanImpl", new BooleanClassFactory(model));
    addFactory("XAttributeContinuousImpl", new ContinuousClassFactory(model));
    addFactory("XAttributeDiscreteImpl", new DiscreteClassFactory(model));
    addFactory("XAttributeTimestampImpl", new TimestampClassFactory(model));
    addFactory("XAttributeDurationImpl", new DurationClassFactory(model));
    addFactory("XAttributeIDImpl", new IDClassFactory(model));
    addFactory("XAttributeContainerImpl", new ContainerClassFactory(model));
    addFactory("XAttributeCollectionImpl", new CollectionClassFactory(model));
    addFactory("XAttributeListImpl", new ListClassFactory(model));
  }
}