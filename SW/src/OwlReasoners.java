import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.PrintUtil;

/**
 * Created by Joep on 16-Mar-17.
 */
public class OwlReasoners {

    public static void main(String[] args) {
        Model schema = FileManager.get().loadModel("C:\\Users\\Joep\\Documents\\GitHub\\DataScience\\SW\\deliverables\\owlDemoSchema_budgetComputer.xml");
        Model data = FileManager.get().loadModel("C:\\Users\\Joep\\Documents\\GitHub\\DataScience\\SW\\deliverables\\owlDemoData_budgetComputer.xml");
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(schema);
        InfModel infmodel = ModelFactory.createInfModel(reasoner, data);

        Resource nForce = infmodel.getResource("urn:x-hp:eg/nForce");
        System.out.println("nForce *:");
        printStatements(infmodel, nForce, null, null);
    }

    public static void printStatements(Model m, Resource s, Property p, Resource o) {
        for (StmtIterator i = m.listStatements(s,p,o); i.hasNext(); ) {
            Statement stmt = i.nextStatement();
            System.out.println(" - " + PrintUtil.print(stmt));
        }
    }
}
