import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.reasoner.ValidityReport;
import org.apache.jena.util.FileManager;
import org.apache.jena.util.PrintUtil;

import java.util.Iterator;

/**
 * Created by Joep on 16-Mar-17.
 */
public class OwlReasoners {

    public static void main(String[] args) {
        Model schema = FileManager.get().loadModel("/Users/peterwessels/Documents/Studie/DataScience/SW/owlDemoSchema_budgetComputer.xml");
        Model data = FileManager.get().loadModel("/Users/peterwessels/Documents/Studie/DataScience/SW/owlDemoData_budgetComputer.xml");
        Reasoner reasoner = ReasonerRegistry.getOWLReasoner();
        reasoner = reasoner.bindSchema(schema);
        InfModel infmodel = ModelFactory.createInfModel(reasoner, data);

        Resource nForce = infmodel.getResource("urn:x-hp:eg/GameBundle");
        System.out.println("GameBundle *:");
        printStatements(infmodel, nForce, null, null);
        System.out.println("Checking validity:");
        checkValidity(infmodel);
    }

    public static void printStatements(Model m, Resource s, Property p, Resource o) {
        for (StmtIterator i = m.listStatements(s,p,o); i.hasNext(); ) {
            Statement stmt = i.nextStatement();
            System.out.println(" - " + PrintUtil.print(stmt));
        }
    }

    public static boolean checkValidity(InfModel model) {
        ValidityReport validity = model.validate();

        if(validity.isValid()) {
            System.out.println("Model is valid");
            return true;
        } else {
            System.out.println("Conflicts");
            for(Iterator i = validity.getReports(); i.hasNext(); ) {
                ValidityReport.Report report = (ValidityReport.Report)i.next();
                System.out.println(" - " + report);
            }
            return false;
        }
    }
}
