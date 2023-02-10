package demo;

import de.uni_trier.wi2.XEStoWorkflowConverter;
import de.uni_trier.wi2.namingUtils.ClassNamePreviewer;
import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;

public class ClassNamePreviewerDemo {

    public static void main(String[] args) throws Exception {
        String filepath = "src/test/resources/example-files/ADT_EXAMPLES.xes";
        ClassNamePreviewer cnp = new ClassNamePreviewer(filepath);
        cnp.printClassNames();

        System.out.println("\n-------------------------------\n");

        CakeInstance.start();
        Model model = ModelFactory.getDefaultModel();
        XEStoWorkflowConverter conv = new XEStoWorkflowConverter(model,filepath);
        conv.getWorkflows();
        conv.printCreatedClasses(true);
    }

}
