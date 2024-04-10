package demo;

import de.uni_trier.wi2.dom.conversion.FileToXESGraphConverter;
import de.uni_trier.wi2.dom.conversion.XESGraphToWorkflowConverter;
import de.uni_trier.wi2.dom.conversion.XESTraceGraph;
import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.nest.NESTWorkflowObject;
import de.uni_trier.wi2.procake.data.objectpool.ObjectPoolFactory;
import de.uni_trier.wi2.procake.data.objectpool.WriteableObjectPool;

import java.io.File;
import java.io.FileWriter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ConversionAndToXMLNewImpl {

    public static void main(String[] args) throws Exception {
        CakeInstance.start();

        Model model = ModelFactory.getDefaultModel();

        String filename1 = "src/test/resources/example-files/ADT_EXAMPLES.xes";
        Collection<XESTraceGraph> graphs = new FileToXESGraphConverter().convert(new File(filename1));

        XESGraphToWorkflowConverter converter = new XESGraphToWorkflowConverter(model);

        WriteableObjectPool pool = ObjectPoolFactory.newObjectPool();
        for (XESTraceGraph graph : graphs) graph.addEdgesByDocumentOrder();
        List<NESTWorkflowObject> workflows = graphs.stream().map(converter::convert).collect(Collectors.toList());
        pool.storeAll(workflows);
        String xml = pool.toXML();
        System.out.println(xml);

        File file = new File("src/test/resources/output/casebase.xml");
        FileWriter fw = new FileWriter(file);
        fw.write(xml);
        fw.close();
    }
}
