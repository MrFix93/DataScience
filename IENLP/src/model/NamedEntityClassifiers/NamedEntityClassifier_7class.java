package model.NamedEntityClassifiers;

/**
 * Created by Joep on 18-Mar-17.
 */
public class NamedEntityClassifier_7class extends NamedEntityClassifier_3class{

    public final static String typeClass = "7class";

    public NamedEntityType DATE;
    public NamedEntityType MONEY;
    public NamedEntityType PERCENT;
    public NamedEntityType TIME;

    public NamedEntityClassifier_7class(){
        super();

        DATE = new NamedEntityType("DATE","<DATE>","</DATE>");
        entityTypes.add(DATE);

        MONEY = new NamedEntityType("MONEY","<MONEY>","</MONEY>");
        entityTypes.add(MONEY);

        PERCENT = new NamedEntityType("PERCENT","<PERCENT>","</PERCENT>");
        entityTypes.add(PERCENT);

        TIME = new NamedEntityType("TIME","<TIME>","</TIME>");
        entityTypes.add(TIME);


    }
}
