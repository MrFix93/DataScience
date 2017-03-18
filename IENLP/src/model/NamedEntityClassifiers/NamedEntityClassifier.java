package model.NamedEntityClassifiers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Joep on 18-Mar-17.
 */
public class NamedEntityClassifier{

    public class NamedEntityType{
        public String TAG;
        public String START;
        public String STOP;

        public NamedEntityType(String tag, String start, String stop){
            this.TAG = tag;
            this.START = start;
            this.STOP = stop;
        }
    }

    public List<NamedEntityType> entityTypes;

    NamedEntityClassifier(){
        entityTypes = new ArrayList<>();
    }
}
