package org.example.demo;

import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.utils.nestworkfloweditor.NESTWorkflowEditor;
import org.example.XEStoWorkflowConverter;

public class NESTWorkflowEditorDemoEmpty {

  public static void main(String[] args) throws Exception {
    CakeInstance.start("composition.xml");

    Model model = ModelFactory.getDefaultModel();
    XEStoWorkflowConverter converter = new XEStoWorkflowConverter(model,"src/main/java/org/example/demo/ADT_EXAMPLES.xes");
    converter.addGlobalTraceAttributes();
    converter.addGlobalEventAttributes();
    converter.setEdgesByDocumentOrder();

    converter.print();

    new NESTWorkflowEditor(converter.getWorkflows()[1]);
    converter.printCreatedClasses();
  }

}
