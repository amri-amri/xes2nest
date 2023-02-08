package demo;

import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject;
import de.uni_trier.wi2.procake.data.objectpool.ObjectPoolFactory;
import de.uni_trier.wi2.procake.data.objectpool.WriteableObjectPool;
import org.example.XEStoWorkflowConverter;

public class ConversionAndToXML {

    public static void main(String[] args) throws Exception {
        CakeInstance.start();

        Model model = ModelFactory.getDefaultModel();

        String filename1 = "src/test/resources/example-files/ADT_EXAMPLES.xes";

        XEStoWorkflowConverter converter = new XEStoWorkflowConverter(model,filename1);

        converter.setEdgesByDocumentOrder();

        NESTWorkflowObject[] workflows = converter.getWorkflows();

        WriteableObjectPool pool = ObjectPoolFactory.newObjectPool();

        for (NESTWorkflowObject workflow : workflows) {
            pool.store(workflow);
        }
        System.out.println(pool.toXML());
    }
}
