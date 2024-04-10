package demo.retrieval;

import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject;
import de.uni_trier.wi2.procake.data.objectpool.ObjectPoolFactory;
import de.uni_trier.wi2.procake.data.objectpool.WriteableObjectPool;
import de.uni_trier.wi2.procake.retrieval.*;
import de.uni_trier.wi2.dom.XEStoWorkflowConverter;

public class ConversionAndRetrieval {
    public static void main(String[] args) throws Exception {
        CakeInstance.start();

        Model model = ModelFactory.getDefaultModel();

        String filename1 = "src/test/resources/example-files/ADT_EXAMPLES.xes";

        XEStoWorkflowConverter converter = new XEStoWorkflowConverter(model,filename1);

        converter.setEdgesByDocumentOrder();

        converter.addGlobalTraceAttributes();
        converter.addGlobalEventAttributes();

        NESTWorkflowObject[] workflows = converter.getWorkflows();

        WriteableObjectPool pool = ObjectPoolFactory.newObjectPool();

        for (NESTWorkflowObject workflow : workflows) {
            pool.store(workflow);
        }

        Retriever graphAStarParallelRetriever = RetrievalFactory.newRetriever(SystemRetrievers.LINEAR_RETRIEVER);

        graphAStarParallelRetriever.setObjectPool(pool);
        Query query = graphAStarParallelRetriever.newQuery();
        query.setQueryObject(workflows[0]);
        query.setRetrieveCases(true);

        RetrievalResultList retrievalResultList = graphAStarParallelRetriever.perform(query);

        for (RetrievalResult result : retrievalResultList){
            System.out.println(result.toString());
        }
    }
}
