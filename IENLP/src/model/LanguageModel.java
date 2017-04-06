package model;

import java.io.*;
import java.util.*;

import static model.Util.cleanTitle;

/**
 * Created by peterwessels on 06/03/2017.
 */
public class LanguageModel {
    Map<String, Double> NGrams = new HashMap<>();
    BufferedReader in;
    String defaultCorpus = "/Users/peterwessels/Documents/Studie/Data Science/IENLP/data/DBLPTrainset(1).txt.corpus";

    public static void main(String[] args) throws IOException {
        LanguageModel lm = new LanguageModel();
        lm.in = new BufferedReader(new InputStreamReader(System.in));
        lm.start();
    }

    public void start() throws IOException {
        boolean run = true;

        System.out.println("Please enter the path + filename of the to-be-used corpus. Enter '1' for the default corpus.");
        String input = this.in.readLine();

        try {
            if(input.equals("1")) {
                this.load(defaultCorpus);
                HashMap<String, Double> result = this.sortByComparator(this.NGrams, false, 4);
                this.printResult(result,50);
            } else {
                this.load(input);
            }
        } catch (FileNotFoundException e) {
            System.out.println("This file could not be found, please try again.");
            this.start();
        }

        while(run) {
            System.out.println("Enter a sentence");
            String sentence = this.in.readLine();
            Double probability = this.sentenceProbability(sentence);
            System.out.println("The probability is " + probability);
        }

    }

    public String load(String fileName) throws FileNotFoundException {
        String result = "";

        Scanner scanner;

        scanner = new Scanner(new File(fileName));
        while(scanner.hasNextLine()) {
            String[] text = scanner.nextLine().split("\\t");
            this.NGrams.put(text[0],Double.parseDouble(text[1]));
        }

        return result;
    }


    public double sentenceProbability(String sentence){
        sentence = LanguageModelMaker.START_DELIMITER + " " + cleanTitle(sentence) + " " + LanguageModelMaker.END_DELIMITER;
        String[] sentenceWords = sentence.split("\\s");

        double prob = 0d;
        wordProb :
        for (int wordIndex = 0; wordIndex < sentenceWords.length; wordIndex++){
            String key = sentenceWords[wordIndex];
            for (int n = 1; n < LanguageModelMaker.MAX_SIZE; n++ ){
                if(wordIndex - n < 0) {
                    continue wordProb;
                }

                key = sentenceWords[wordIndex - n] + " " + key;

                //System.out.println("the word is: " + key);
                if(this.NGrams.containsKey(key)){
                    double wordProb = this.NGrams.get(key);
                    //System.out.println("the prob of " + key + " is " + wordProb);
                    prob += Math.log10(wordProb);
                }
            }
        }
        return prob;
    }

    /**
     *
     * @param unsortMap
     * @param order
     * @return
     */
    private static HashMap<String, Double> sortByComparator(Map<String, Double> unsortMap, final boolean order, int n)
    {

        List<Map.Entry<String, Double>> list = new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>()
        {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list)
        {
            int length = entry.getKey().split("\\s").length;
            if(length == n) {
                sortedMap.put(entry.getKey(), entry.getValue());
            }
        }
        return sortedMap;
    }

    private static void printResult(Map<String, Double> mapToPrint, int limit) {
        int count = 0;
        for (Map.Entry<String, Double> entry:mapToPrint.entrySet()) {
            System.out.println(entry.getKey() + "," + entry.getValue());
            if(count >= limit) {
                return;
            }
            count++;
        }
    }

}
