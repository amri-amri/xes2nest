package demo.retrieval;

import de.uni_trier.wi2.procake.data.model.DataClass;
import de.uni_trier.wi2.procake.data.object.DataObject;
import de.uni_trier.wi2.procake.data.object.base.AggregateObject;
import de.uni_trier.wi2.procake.data.object.base.StringObject;
import de.uni_trier.wi2.procake.similarity.Similarity;
import de.uni_trier.wi2.procake.similarity.SimilarityValuator;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityImpl;
import de.uni_trier.wi2.procake.similarity.impl.SimilarityMeasureImpl;

public class BaseClassSimilarityMeasure extends SimilarityMeasureImpl {

    public static final String NAME = "BaseClassMeasure";

    @Override
    public boolean isSimilarityFor(DataClass dataclass, String orderName) {
        for (DataClass c: dataclass.getSuperClasses()) {
            if (c.getName().equals("XESBaseClass")) return true;
        }
        return dataclass.getName().equals("XESBaseClass");
    }

    @Override
    public Similarity compute(DataObject queryObject, DataObject caseObject, SimilarityValuator valuator) {
        AggregateObject o1 = (AggregateObject) queryObject;
        AggregateObject o2 = (AggregateObject) caseObject;
        double simVal = 0;
        if (((StringObject) o1.getAttributeValue("key")).getNativeString().equals(((StringObject)o2.getAttributeValue("key")).getNativeString())) {
            simVal = valuator.computeSimilarity(o1.getAttributeValue("value"), o2.getAttributeValue("value")).getValue();
        }
        return new SimilarityImpl(this, queryObject, caseObject, simVal);
    }


    @Override
    public String getSystemName() {
        return NAME;
    }
}

