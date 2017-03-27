import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.sequences.SeqClassifierFlags;
import edu.stanford.nlp.util.StringUtils;

import java.io.*;
import java.util.Properties;

/**
 * Created by peterwessels on 22/03/2017.
 */
public class ClassifierTrainer {
    private int k;
    private String training_data;
    private String classifier;
    private String template;
    public static final String PROPERTIESFORMAT = "ner/prop/properties%s_part%s.prop";


    public static void main(String[] args) throws IOException {
        ClassifierTrainer ct = new ClassifierTrainer(Integer.parseInt(args[1]));
        ct.prepareFiles(args[0]);
        ct.setClassifier(args[2]);
        ct.createPropertiesFiles("template.prop");
        ct.trainClassifier();
    }

    public ClassifierTrainer(int k) {
        this.k = k;
    }

    public void setClassifier(String prefix) {
        this.classifier = prefix + k + "kfold.ser.gz";
    }

    public void prepareFiles(String input) {
        try {
            SplitFile.generateTrainingTestFiles(this.k, input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTestData(int part) {
        String path = String.format(SplitFile.TESTFORMAT, k, part);
        try {
            FileReader file = new FileReader(path);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return path;
    }

    public String getTrainingData(int part) {
        String path = String.format(SplitFile.TRAININGFORMAT, k, part);
        try {
            FileReader file = new FileReader(path);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return path;
    }

    public String getProperties(int part) {
        String path = String.format(ClassifierTrainer.PROPERTIESFORMAT, k, part);
        try {
            FileReader file = new FileReader(path);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return path;
    }

    public void validateResult(int part) {
        String command = String.format("-loadClassifier %s -testFile %s", this.classifier, this.getTestData(part));
        String[] commands = command.split( " ");
        try {
            System.out.println("----- START TESTING PART " + part + " ------");
            CRFClassifier.main(commands);
            System.out.println("----- END TESTING PART " + part + " ------");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createPropertiesFiles(String templateFile) throws IOException {
        for(int i = 1; i <= k; i++) {
            this.createPropertiesFile(templateFile, i);
        }
    }

    public void createPropertiesFile(String templateFile, int part) throws IOException {
        PrintWriter prop = new PrintWriter(getProperties(part), "UTF-8");
        BufferedReader template = new BufferedReader(new FileReader("ner/prop/" + templateFile));

        String trainingfile = String.format("trainFile = %s", this.getTrainingData(part), 1);
        String classifier = String.format("serializeTo = %s", this.classifier, 1);

        prop.println(trainingfile);
        prop.println(classifier);

        String line;
        while ((line = template.readLine()) != null) {
            prop.println(line);
        }

        prop.close();
    }

    public void trainClassifier() {
        for(int i = 1; i <= k; i++) {
            String command = String.format("-prop %s", this.getProperties(i));
            String[] commands = command.split( " ");
            try {
                CRFClassifier.main(commands);
                validateResult(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
