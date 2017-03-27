import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by peterwessels on 20/03/2017.
 */
public class SplitFile {
    public static final String TRAININGFORMAT = "output/training_tweets%s_part%s.tok";
    public static final String TESTFORMAT = "output/test_tweets%s_part%s.tok";
    public static final String[] search = {"B-PER", "I-PER", "B-ORG", "B-Org", "I-ORG", "I-LOC", "B-LOC", "I-MISC", "B-MISC"};
    public static final String[] replace = {"PER", "PER", "ORG", "ORG", "ORG", "LOC", "LOC", "MISC", "MISC"};
    public static final String[] labels = {"Entity", "P", "R", "F1", "TP", "FP", "FN"};
    /**
     * Tokenizes tweet texts on standard input, tokenizations on standard output.  Input and output UTF-8.
     */
    public static void main(String[] args) throws IOException {

        SplitFile.combineResults(args[0], Integer.parseInt(args[1]));
    }


    public static void generateTrainingTestFiles(int k, String inputFile) throws IOException {

        int bucketSize = Twokenizer.countLines(inputFile) / k;

        k_loop: for(int i = 0; i < k; i++) {
            int excludeMinThreshold = bucketSize * i;
            int excludeMaxThreshold = excludeMinThreshold + bucketSize;

            BufferedReader input = new BufferedReader(new FileReader(inputFile));
            PrintWriter output_training = new PrintWriter(String.format(TRAININGFORMAT, k, (i+1)), "UTF-8");
            PrintWriter output_test = new PrintWriter(String.format(TESTFORMAT, k, (i+1)), "UTF-8");

            int countLines = 0;
            String line;
            while ((line = input.readLine()) != null) {
                countLines++;
                if (countLines > excludeMinThreshold && countLines < excludeMaxThreshold) {
                    output_test.println(filterClasses(line));
                } else {
                    output_training.println(filterClasses(line));
                }

            }
            output_test.close();
            output_training.close();
        }
    }

    public static String filterClasses(String input) {
        return StringUtils.replaceEach(input, search, replace);
    }

    public static void combineResults(String inputFile, int k) throws IOException {
        System.out.println("K=" + k);
        try {
            BufferedReader input = new BufferedReader(new FileReader(inputFile));

            HashMap<String, HashMap<String, Double>> results = new HashMap<>();
            String line;
            while((line = input.readLine()) != null) {
                String[] tabs = line.split("\\t");
                if(tabs.length != 7) continue;
                if(tabs[0].equals("Entity")) continue;

                String label = tabs[0].trim();

                if(!results.containsKey(label)) {
                    results.put(label, new HashMap<>());
                    for(int i = 1; i < tabs.length; i++) {
                        results.get(label).put(labels[i], 0d);
                    }
                }

                for(int i = 1; i < tabs.length; i++) {
                    Double current = results.get(label).get(labels[i]);
                    System.out.println(labels[i] + "= " + current);
                    results.get(label).put(
                            labels[i],
                            (Double.parseDouble(tabs[i]) + current)
                    );
                }
            }

            HashMap<String, HashMap<String, Double>> finalResults = new HashMap<>();
            for(Map.Entry<String, HashMap<String, Double>> entry : results.entrySet()) {
                HashMap<String, Double> avgResult = new HashMap<>();
                for(Map.Entry<String, Double> res : entry.getValue().entrySet()) {
                    Double avg = res.getValue() / k;
                    avgResult.put(res.getKey(), avg);
                }
                finalResults.put(entry.getKey(), avgResult);
            }
            System.out.println(results);
            formatTable(finalResults);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void formatTable(HashMap<String, HashMap<String, Double>> mapToFormat) {
        System.out.format("%20s\t\t\t%2s\t\t\t%2s\t\t\t%2s\t\t\t%2s\t\t\t%2s\t\t\t%2s\n", labels);

        for(Map.Entry<String, HashMap<String, Double>> entry : mapToFormat.entrySet()) {
            Double[] digits = {0d,0d,0d,0d,0d,0d,0d};
            int count = 0;
            for(Map.Entry<String, Double> d : entry.getValue().entrySet()) {
                digits[count] = d.getValue();
                count++;
            }
            System.out.printf("%20s\t\t" +
                    "%.4f\t\t%.4f\t\t%.4f\t\t%.4f\t\t%.4f\t\t%.4f \n",
                    entry.getKey(), digits[0],digits[1],digits[4],digits[5],digits[3],digits[2]);
        }

    }

}