package model.NamedEntityClassifiers;

/**
 * Created by Joep on 18-Mar-17.
 */
public class NamedEntityClassifier_4class extends NamedEntityClassifier_3class{

    public final static String typeClass = "4class";

    public NamedEntityType MISC;

    public NamedEntityClassifier_4class(){
        super();

        MISC = new NamedEntityType("MISC","<MISC>","</MISC>");
        entityTypes.add(MISC);

    }
}
