package demo;

import de.uni_trier.wi2.*;
import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject;
import de.uni_trier.wi2.procake.utils.nestworkfloweditor.NESTWorkflowEditor;

import java.io.File;

public class ConversionAndNESTWorkflowEditorNewDemo {

    public static void main(String[] args) {
        CakeInstance.start();

        Model model = ModelFactory.getDefaultModel();

        String filename1 = "src/test/resources/example-files/ADT_EXAMPLES.xes";
        XESTraceGraph graph = (XESTraceGraph) new FileToXESGraphConverter().convert(new File(filename1)).toArray()[0];
        graph.addEdgesByDocumentOrder();

        XESGraphToWorkflowConverter converter = new XESGraphToWorkflowConverter(model);

        NESTWorkflowObject workflow = converter.convert(graph);

        new NESTWorkflowEditor(workflow);
    }
}
