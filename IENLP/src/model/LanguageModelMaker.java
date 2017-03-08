package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import static model.Util.cleanTitle;
import static model.Util.sortByComparator;


public class LanguageModelMaker {
    final static int MAX_SIZE = 4;
    final static int AMOUNT_RECORDS =  100;
    final static int RESULT_SIZE = 4;
    final static int MAX_RESULTS = 50;
    final static String END_DELIMITER = "TITLEEND";
    final static String START_DELIMITER = "TITLESTART";
    private String all_words = "";
    private List<NGram> AllNGrams;
    private HashMap<String, Double> NGramsProbability;

    public static void main(String[] args) {
        LanguageModelMaker lmm = new LanguageModelMaker();
        lmm.start(args[0], args[1]);
    }

    public void start(String path, String filename) {
        Util.log("verbose", "Starting application");
        Util.log("init", "Max size n-grams: " + MAX_SIZE);
        Util.log("init", "Amount of records: " + AMOUNT_RECORDS);

        Util.log("verbose", "Start reading file " + path + filename);
        all_words = readFile(path + filename);

        Util.log("verbose", "Creating NGrams..");
        this.AllNGrams = createNGrams(all_words);
        int[] counts = nGramCount(AllNGrams);

        Util.log("verbose", "Calculating probability");
        this.NGramsProbability = new HashMap<>();
        for(NGram ngram: AllNGrams) {
            // Calculate the P(n|n-1) for every NGrams and add to the list
            this.NGramsProbability.putAll(ngram.getAllP(counts));
        }
        Util.log("verbose", "Amount of n-grams found: " + this.NGramsProbability.size());

        this.save(path, filename);

        SentenceModel sentenceModel = new SentenceModel(this.NGramsProbability,"Dimensioning an OBS Switch with Partial Wavelength Conversion and Fiber Delay Lines via a Mean Field Model");
        List<String[]> test = sentenceModel.createSentenceSegments();

        for (String[] segment: test) {
            for (String word: segment) {
                System.out.print(word + " ");
            }
            System.out.println(" prob: " + sentenceModel.segmentStickyness(segment));
        }


        /*
        //System.out.println("FINISHED NGramsProbability; with " + NGramsProbability.size() + " NGrams and probabilities");

        //System.out.println("START sortByComparator");
        HashMap<String, Double> sorted = sortByComparator(NGramsProbability,false);
        //System.out.println("FINISHED sortByComparator;");

        //System.out.println("START createResultList");
        HashMap<Integer, List<String>> results = createResultList(sorted);
        //System.out.println("FINISH createResultList");

        for(int i = 2; i <= MAX_SIZE; i++) {
            System.out.println("TOP " + MAX_RESULTS + " of " + i + "-gram");
            for(String print: results.get(i)){
                System.out.println(print);
            }
            System.out.println("END TOP " + MAX_RESULTS + " of " + i + "-gram");
        }
        */

        Util.log("verbose", "Application finished");
    }

    public static HashMap<Integer, List<String>> createResultList(HashMap<String, Double> sorted){
        HashMap<Integer, List<String>> results = new HashMap<>();

        for(int i = 2; i <= MAX_SIZE; i++) {//create object for every n-gram
            results.put(i, new ArrayList<>());
        }

        for(Map.Entry<String, Double> entry: sorted.entrySet()) {
            int n = entry.getKey().split("\\s").length;//get the N of the current n-gram

            int current_length = results.get(n).size(); //get the size of the current n-gram resulting List
            if(current_length <= MAX_RESULTS) { //add to the list if the list is not at its maximum size
                results.get(n).add("(" + current_length + ") " + entry.getKey() + ": " + entry.getValue());
            }

            //stop loop if all resulting list are filled
            int full = 0;
            for(int i = 2; i <= MAX_SIZE; i++) {
                if(results.get(i).size() == MAX_RESULTS) {
                    full++;
                }
            }
            if(full == MAX_SIZE-2){
                break;
            }
        }
        return results;
    }

    public static int[] nGramCount(List<NGram> ALlNGrams){
        int[] counts = new int[MAX_SIZE];

        for(NGram ngram: ALlNGrams) {
            for(int i = 0; i < ngram.getLength(); i++) {
                counts[i]++;
            }
        }
        return counts;
    }

    public List<NGram> createNGrams(String input) {
        String[] words = input.split("\\s");

        List<NGram> ngrams = new ArrayList<>();

        ngram_key_loop:
        for(int i = 0;i < words.length; i++) { //iterates over all seperate words
            String key = "";
            if(words[i].equals(LanguageModelMaker.END_DELIMITER)) {
                continue ngram_key_loop;
            }

            NGram newGram = new NGram(this.all_words);
            for(int w = 0; w < MAX_SIZE; w++) { //generate the n-gram classification as key; if 1-gram key is one word, etc
                String next = words[i + w];
                if(next.equals(LanguageModelMaker.END_DELIMITER)) {
                    newGram.add(key + LanguageModelMaker.END_DELIMITER);
                    ngrams.add(newGram);
                    continue ngram_key_loop;
                }
                key += next + " ";

                newGram.add(key);
            }

            ngrams.add(newGram);
        }

        return ngrams;
    }

    public boolean save(String path, String filename) {
        try {
            String new_file = filename.split("/.")[0].concat(".corpus");

            Util.log("verbose", "Creating corpus in file: " + new_file);

            FileWriter file = new FileWriter(path + new_file);
            for(Map.Entry<String, Double> ngram : this.NGramsProbability.entrySet()) {
                String line = ngram.getKey() + "\t" + ngram.getValue();
                file.write(line + "\n");
            }
            file.close();
        } catch (IOException e) {
            Util.log("error", e.getMessage());
            e.printStackTrace();
        }
        return true;
    }

    /**
     *
     * @param fileName
     * @return
     */
    public static String readFile(String fileName){
        String result = "";

        Scanner scanner;
        try {
            scanner = new Scanner(new File(fileName));
            int records = AMOUNT_RECORDS;
            while(scanner.hasNextLine()) {
                if(records-- < 0) {
                    break;
                }

                String[] text = scanner.nextLine().split("\\t");
                String title = text[2];
                title = cleanTitle(title);

                result += LanguageModelMaker.START_DELIMITER + " " + title + " " + LanguageModelMaker.END_DELIMITER + " ";
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }
}
