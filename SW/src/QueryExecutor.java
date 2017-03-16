/**
 * Created by peterwessels on 15/02/2017.
 */
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;

public class QueryExecutor {

    private Model model;
    private ArrayList<Query> queries = new ArrayList<Query>();
    private BufferedReader in;

    public static void main(String[] args){
        System.out.println("Welcome to the RDF query executor \n> ");

        QueryExecutor qe = new QueryExecutor();
        qe.in = new BufferedReader(new InputStreamReader(System.in));
        qe.model = ModelFactory.createDefaultModel();
        qe.start();
    }

    private void start() {
        boolean running = true;

        String[] input, cargs;
        String command;
        while(running) {
            try {
                input = this.in.readLine().split(" ");
                command = input[0];
                cargs = Arrays.copyOfRange(input, 1, input.length);

                switch (command) {
                    case "model":
                        this.setModel(cargs[0]);
                        break;
                    case "query":
                        this.addquery(cargs[0]);
                        break;
                    case "execute":
                        this.executeAll();
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setModel(String model) {
        this.model.read(model);
    }

    private void addquery(String inputquery) {
        Query query = QueryFactory.read(inputquery);
        this.queries.add(query);
    }

    private void executeAll() {
        Iterator iterator = this.queries.iterator();

        while(iterator.hasNext()) {
            this.execute((Query)iterator.next());
        }
    }

    private void execute(Query query) {
        try (QueryExecution qexec = QueryExecutionFactory.create(query, this.model)) {
            ResultSet results = qexec.execSelect() ;
            for ( ; results.hasNext() ; )
            {
                QuerySolution soln = results.nextSolution() ;
                RDFNode x = soln.get("varName") ;       // Get a result variable by name.
                Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
                Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
                ResultSetFormatter.out(System.out, results, query) ;
            }
        }
    }

}




