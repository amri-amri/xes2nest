package demo.retrieval;

import de.uni_trier.wi2.conversion.FileToXESGraphConverter;
import de.uni_trier.wi2.conversion.XESGraphToWorkflowConverter;
import de.uni_trier.wi2.conversion.XESTraceGraph;
import de.uni_trier.wi2.namingUtils.Classnames;
import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject;
import de.uni_trier.wi2.procake.data.object.nest.utils.impl.NESTSequentialWorkflowValidatorImpl;
import de.uni_trier.wi2.procake.data.objectpool.ObjectPoolFactory;
import de.uni_trier.wi2.procake.data.objectpool.WriteableObjectPool;
import de.uni_trier.wi2.procake.retrieval.*;
import de.uni_trier.wi2.procake.similarity.SimilarityModel;
import de.uni_trier.wi2.procake.similarity.SimilarityModelFactory;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.base.collection.SMCollectionIsolatedMapping;
import de.uni_trier.wi2.procake.similarity.base.string.SMStringLevenshtein;
import de.uni_trier.wi2.procake.similarity.nest.astar.SMGraphAStarThree;
import de.uni_trier.wi2.procake.similarity.nest.sequence.SMGraphSWA;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ConversionAndRetrievalNewImpl {
    public static void main(String[] args) {
        CakeInstance.start();

        Model model = ModelFactory.getDefaultModel();
        SimilarityValuator simVal = SimilarityModelFactory.newSimilarityValuator();
        String filename1 = "src/test/resources/example-files/ADT_EXAMPLES.xes";
        Collection<XESTraceGraph> graphs = new FileToXESGraphConverter().convert(new File(filename1));

        XESGraphToWorkflowConverter converter = new XESGraphToWorkflowConverter(model);

        WriteableObjectPool<NESTWorkflowObject> pool = ObjectPoolFactory.newObjectPool();
        for (XESTraceGraph graph : graphs) graph.addEdgesByDocumentOrder();
        System.out.println("Converting Graphs");
        List<NESTWorkflowObject> workflows = graphs.stream().map(converter::convert).collect(Collectors.toList());
        System.out.println("Conversion done");
        List<NESTSequentialWorkflowObject> seqWorkflows = new ArrayList<>();
        for (NESTWorkflowObject e: workflows) {
            NESTSequentialWorkflowObject ne = (NESTSequentialWorkflowObject) model.getNESTSequentialWorkflowClass().newObject();
            ne.transformNESTGraphToNESTSequentialWorkflow(e);
            seqWorkflows.add(ne);
        }
        NESTSequentialWorkflowObject seqWorkflow = (NESTSequentialWorkflowObject) model.getNESTSequentialWorkflowClass().newObject();
        seqWorkflow.transformNESTGraphToNESTSequentialWorkflow(workflows.get(0));
        System.out.println("Graph is sequential Workflow:" + new NESTSequentialWorkflowValidatorImpl(workflows.get(0)).isValidSequentialWorkflow());
        addSimilarityMeasures(model, simVal);
        pool.storeAll(workflows);
        Retriever retriever = RetrievalFactory.newRetriever(SystemRetrievers.GRAPH_ASTAR_PARALLEL_RETRIEVER);
        retriever.setSimilarityModel(simVal.getSimilarityModel());
        retriever.setObjectPool(pool);
        Query query = retriever.newQuery();
        query.setQueryObject(workflows.get(0));
        query.setRetrieveCases(true);
        System.out.println("Retrieving Query Results");
        long starttime = System.currentTimeMillis();
        //System.out.println(simVal.computeSimilarity(seqWorkflows.get(0), seqWorkflows.get(1)).toDetailedString());
        RetrievalResultList retrievalResultList = retriever.perform(query);
        long endtime = System.currentTimeMillis();
        System.out.println((endtime - starttime)/1000);
        for (RetrievalResult result : retrievalResultList){
            System.out.println(result.toString());
        }
    }

    private static void addSimilarityMeasures(Model model, SimilarityValuator simVal) {
        SimilarityModel similarityModel = simVal.getSimilarityModel();
        similarityModel.registerSimilarityMeasureTemplate(new BaseClassSimilarityMeasure());
        BaseClassSimilarityMeasure baseMeasure = (BaseClassSimilarityMeasure) similarityModel.createSimilarityMeasure(BaseClassSimilarityMeasure.NAME, model.getClass(Classnames.BASE));
        similarityModel.addSimilarityMeasure(baseMeasure, BaseClassSimilarityMeasure.NAME);
        similarityModel.setDefaultSimilarityMeasure(model.getClass(Classnames.BASE), BaseClassSimilarityMeasure.NAME);
        similarityModel.registerSimilarityMeasureTemplate(new UnnaturallyNestedClassSimilarityMeasure());
        UnnaturallyNestedClassSimilarityMeasure class1Measure = (UnnaturallyNestedClassSimilarityMeasure) similarityModel.createSimilarityMeasure(UnnaturallyNestedClassSimilarityMeasure.NAME, model.getClass(Classnames.UNNATURALLY_NESTED));
        similarityModel.addSimilarityMeasure(class1Measure, UnnaturallyNestedClassSimilarityMeasure.NAME);
        similarityModel.setDefaultSimilarityMeasure(model.getClass(Classnames.UNNATURALLY_NESTED), UnnaturallyNestedClassSimilarityMeasure.NAME);
        SMCollectionIsolatedMapping smCollectionMapping = (SMCollectionIsolatedMapping) similarityModel.createSimilarityMeasure(SMCollectionIsolatedMapping.NAME, model.getSetSystemClass());
        similarityModel.addSimilarityMeasure(smCollectionMapping, "SMCollectionIsolatedMapping");
        similarityModel.setDefaultSimilarityMeasure(model.getSetSystemClass(), "SMCollectionIsolatedMapping");
        SMStringLevenshtein smStringLevenshtein = (SMStringLevenshtein) similarityModel.createSimilarityMeasure(SMStringLevenshtein.NAME, model.getStringSystemClass());
        smStringLevenshtein.setCaseInsensitive();
        similarityModel.addSimilarityMeasure(smStringLevenshtein, "SMStringLevenshtein");
        similarityModel.setDefaultSimilarityMeasure(model.getStringSystemClass(), "SMStringLevenshtein");
        SMGraphAStarThree smGraphAStarThree = (SMGraphAStarThree) similarityModel.createSimilarityMeasure(SMGraphAStarThree.NAME, model.getNESTWorkflowClass());
        smGraphAStarThree.setReturnLocalSimilarities(false);
        smGraphAStarThree.setMaxQueueSize(5);
        similarityModel.addSimilarityMeasure(smGraphAStarThree, "SMGraphAStarThree");
        SMGraphSWA smGraphSWA = (SMGraphSWA) similarityModel.createSimilarityMeasure(SMGraphSWA.NAME, model.getNESTSequentialWorkflowClass());
        similarityModel.addSimilarityMeasure(smGraphSWA, "SMGraphSWA");
    }
}
