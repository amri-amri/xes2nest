package demo;

import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject;
import de.uni_trier.wi2.procake.utils.nestworkfloweditor.NESTWorkflowEditor;
import org.example.XEStoWorkflowConverter;

public class ConversionAndNESTWorkflowEditorDemo {

    public static void main(String[] args) throws Exception {
        CakeInstance.start();

        Model model = ModelFactory.getDefaultModel();

        String filename1 = "src/test/example files/lifecycle_example.xes";

        XEStoWorkflowConverter.addXESClasses(model);

        XEStoWorkflowConverter converter = new XEStoWorkflowConverter(model,filename1);

        converter.addLifecycleStandardEdges();

        NESTWorkflowObject lifecycle = converter.getWorkflows()[0];

        new NESTWorkflowEditor(lifecycle);
    }
}
