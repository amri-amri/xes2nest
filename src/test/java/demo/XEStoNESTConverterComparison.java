package demo;

import de.uni_trier.wi2.conversion.sax.XEStoNESTsAXConverter;
import de.uni_trier.wi2.procake.CakeInstance;
import de.uni_trier.wi2.procake.data.model.ModelFactory;
import de.uni_trier.wi2.procake.data.object.nest.NESTSequentialWorkflowObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class XEStoNESTConverterComparison {

    static final String xes;

    static {
        try {
            xes = Files.readString(Path.of("src/test/resources/example-files/ADT_EXAMPLES.xes"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static ArrayList<NESTSequentialWorkflowObject> convertSAX() {
        XEStoNESTsAXConverter converter = new XEStoNESTsAXConverter(ModelFactory.getDefaultModel());
        converter.configure(false);
        return converter.convert(xes);
    }

    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        CakeInstance.start();

        long start;
        long finish;
        long timeElapsedSax;
        long totalTimeElapsedSax = 0;

        int M = 2000;

        for (int i = 0; i < M; i++) {
            start = System.currentTimeMillis();
            ArrayList<NESTSequentialWorkflowObject> sax = convertSAX();
            finish = System.currentTimeMillis();
            timeElapsedSax = finish - start;

            totalTimeElapsedSax += timeElapsedSax;
        }

        System.out.printf("Average time: %.3f seconds%n", ((float) totalTimeElapsedSax) / M / 1000);

    }

}
