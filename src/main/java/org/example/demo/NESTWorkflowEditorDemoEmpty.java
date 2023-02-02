package org.example.demo;

import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.Model;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.utils.nestworkfloweditor.NESTWorkflowEditor;
import org.example.XEStoWorkflowConverter;
import org.example.classFactories.ClassFactory;
import org.example.utils.ClassNamePreviewer;

public class NESTWorkflowEditorDemoEmpty {

  private final static String  path = "src/main/java/org/example/demo/activitylog_uci_detailed_weekends.xes";

  public static void main(String[] args) throws Exception {
    CakeInstance.start("composition.xml");

    Model model = ModelFactory.getDefaultModel();
    XEStoWorkflowConverter converter = new XEStoWorkflowConverter(model,path);
    converter.addGlobalTraceAttributes();
    converter.addGlobalEventAttributes();
    converter.setEdgesByDocumentOrder();

    converter.print();

    new NESTWorkflowEditor(converter.getWorkflows()[1]);
    converter.printCreatedClasses(true);
    ClassNamePreviewer preview = new ClassNamePreviewer(path);
    preview.printClassNames();
    preview.getDifferingClasses(converter.getCreatedClasses());
  }

}
