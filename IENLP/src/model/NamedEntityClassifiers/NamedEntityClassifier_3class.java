package model.NamedEntityClassifiers;

/**
 * Created by Joep on 18-Mar-17.
 */
public class NamedEntityClassifier_3class extends NamedEntityClassifier{

    public final static String typeClass = "3class";

    public NamedEntityType ORGANISATION;
    public NamedEntityType LOCATION;
    public NamedEntityType PERSON;

    public NamedEntityClassifier_3class(){
        super();

        ORGANISATION = new NamedEntityType("ORG","<ORGANIZATION>","</ORGANIZATION>");
        entityTypes.add(ORGANISATION);

        LOCATION = new NamedEntityType("LOC","<LOCATION>","</LOCATION>");
        entityTypes.add(LOCATION);

        PERSON = new NamedEntityType("PER","<PERSON>","</PERSON>");
        entityTypes.add(PERSON);

    }
}
