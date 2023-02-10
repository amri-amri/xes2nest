package demo;

import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject;
import de.uni_trier.wi2.procake.utils.nestworkfloweditor.NESTWorkflowEditor;
import de.uni_trier.wi2.XEStoWorkflowConverter;

public class ConversionAndNESTWorkflowEditorDemo {

    public static void main(String[] args) throws Exception {
        CakeInstance.start();

        Model model = ModelFactory.getDefaultModel();

        String filename1 = "src/test/resources/example-files/ADT_EXAMPLES.xes";

        XEStoWorkflowConverter converter = new XEStoWorkflowConverter(model,filename1);

        converter.setEdgesByDocumentOrder();

        converter.addGlobalTraceAttributes();
        converter.addGlobalEventAttributes();

        NESTWorkflowObject lifecycle = converter.getWorkflows()[0];

        new NESTWorkflowEditor(lifecycle);
    }
}
