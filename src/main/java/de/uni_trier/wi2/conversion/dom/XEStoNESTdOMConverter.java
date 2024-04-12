package de.uni_trier.wi2.conversion.dom;

import de.uni_trier.wi2.conversion.AbstractXEStoNESTConverter;
import de.uni_trier.wi2.conversion.dom.conversion.FileToXESGraphConverter;
import de.uni_trier.wi2.conversion.dom.conversion.XESGraphToWorkflowConverter;
import de.uni_trier.wi2.conversion.dom.conversion.XESTraceGraph;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject;
import de.uni_trier.wi2.utils.classFactories.*;

import java.util.ArrayList;
import java.util.HashMap;

@Deprecated
public class XEStoNESTdOMConverter extends AbstractXEStoNESTConverter {
    public XEStoNESTdOMConverter(Model model) {
        super(model);
    }

    @Override
    protected void initializeFactories() {
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

    @Override
    public ArrayList<NESTSequentialWorkflowObject> convert(String xes) {
        // Initialize the two required converters
        FileToXESGraphConverter fileToXESGraphConverter = new FileToXESGraphConverter(addGlobals);
        XESGraphToWorkflowConverter xesGraphToWorkflowConverter = new XESGraphToWorkflowConverter(model, factories);

        // Convert the XES to a list of XESTraceGraphs
        ArrayList<XESTraceGraph> graphs;
        try {
            graphs = fileToXESGraphConverter.convert(xes);
        } catch (Exception e) {
            logger.error("Exception while parsing xes string! Exception: {}", e.getMessage());
            return null;
        }

        // Iterate through list of XESTraceGraphs and convert them to NESTSequentialWorkflows
        ArrayList<NESTSequentialWorkflowObject> sequentialWorkflows = new ArrayList<>();
        for (XESTraceGraph graph : graphs) {
            // An XES contains a log where the order of events represents their order in time
            graph.addEdgesByDocumentOrder();

            // Convert the XESTraceGraph to a NESTWorkflowObject and then to a NESTSequentialWorkflowObject
            NESTWorkflowObject workflow = xesGraphToWorkflowConverter.convert(graph);
            NESTSequentialWorkflowObject sequentialWorkflow = (NESTSequentialWorkflowObject) model.getNESTSequentialWorkflowClass().newObject();
            sequentialWorkflow.transformNESTGraphToNESTSequentialWorkflow(workflow);

            // Add the Workflow to the list of Workflows
            sequentialWorkflows.add(sequentialWorkflow);
        }

        return sequentialWorkflows;
    }
}
