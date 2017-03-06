package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;

import static jdk.nashorn.internal.objects.NativeString.trim;


public class NGramAnalyzer {
    final static int MAX_SIZE = 4;
    final static int AMOUNT_RECORDS =  2000;
    final static int RESULT_SIZE = 4;
    final static int MAX_RESULTS = 50;
    final static String END_DELIMITER = "TITLEEND";
    final static String START_DELIMITER = "TITLESTART";
    static String all_words = "";

    public static void main(String[] args) {

        System.out.println("START readFile");
        all_words = readFile(args[0]);
        System.out.println("FINISHED readFile; with " + all_words.length() + " characters");

        System.out.println("START createNGrams");
        List<NGram> ALlNGrams = createNGrams(all_words);
        System.out.println("FINISHED createNGrams; with " + ALlNGrams.size() + " NGram.class objects");

        System.out.println("START nGramCount");
        int[] counts = nGramCount(ALlNGrams);
        System.out.println("FINISHED nGramCount;");

        System.out.println("START NGramsProbability");
        HashMap<String, Double> NGramsProbability = new HashMap<>();
        for(NGram ngram: ALlNGrams) {
            // Calculate the P(n|n-1) for every NGrams and add to the list
            NGramsProbability.putAll(ngram.getAllP(counts));
        }
        System.out.println("FINISHED NGramsProbability; with " + NGramsProbability.size() + " NGrams and probabilities");

        sentenceProbability(NGramsProbability,"A study of identifibility for blind source separation via nonorthogonal joint diagonalization");
        sentenceProbability(NGramsProbability,"A study of identifibility for blind source separation via nonorthogonal joint applications");


        System.out.println("START sortByComparator");
        HashMap<String, Double> sorted = sortByComparator(NGramsProbability,false);
        System.out.println("FINISHED sortByComparator;");

        System.out.println("START createResultList");
        HashMap<Integer, List<String>> results = createResultList(sorted);
        System.out.println("FINISH createResultList");

        for(int i = 2; i <= MAX_SIZE; i++) {
            System.out.println("TOP " + MAX_RESULTS + " of " + i + "-gram");
            for(String print: results.get(i)){
                System.out.println(print);
            }
            System.out.println("END TOP " + MAX_RESULTS + " of " + i + "-gram");
        }

        System.out.println("finished!");
    }

    public static void sentenceProbability(HashMap<String, Double> NGramsProbability, String sentence){
        sentence = START_DELIMITER + " " + cleanTitle(sentence) + " " + END_DELIMITER;
        String[] sentenceWords = sentence.split("\\s");

        double prob = 0d;
        wordProb :
        for (int wordIndex = 0; wordIndex < sentenceWords.length; wordIndex++){
            String key = sentenceWords[wordIndex];
            for (int n = 1; n < MAX_SIZE; n++ ){
                if(wordIndex - n < 0) {
                    continue wordProb;
                }

                key = sentenceWords[wordIndex - n] + " " + key;

                //System.out.println("the word is: " + key);
                if(NGramsProbability.containsKey(key)){
                    double wordProb = NGramsProbability.get(key);
                    //System.out.println("the prob of " + key + " is " + wordProb);
                    prob += Math.log10(wordProb);
                }
            }
        }
        //System.out.println("The probability for the sentence " + sentence + " is " + prob);

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
        int totalNGrams = 0;
        for(NGram ngram: ALlNGrams) {
            for(int i = 0; i < ngram.getLength(); i++) {
                counts[i]++;
                totalNGrams++;
            }
        }

        System.out.println("Total amount of NGrams: " + totalNGrams);
        return counts;
    }

    public static List<NGram> createNGrams(String input){

        String[] words = input.split("\\s");
        System.out.println("amount of words: " + words.length);

        List<NGram> ngrams = new ArrayList<>();

        ngram_key_loop:
        for(int i = 0;i < words.length; i++) { //iterates over all seperate words
            String key = "";
            if(words[i].equals(NGramAnalyzer.END_DELIMITER)) {
                continue ngram_key_loop;
            }

            NGram newGram = new NGram(all_words);
            for(int w = 0; w < MAX_SIZE; w++) { //generate the n-gram classification as key; if 1-gram key is one word, etc
                String next = words[i + w];
                if(next.equals(NGramAnalyzer.END_DELIMITER)) {
                    newGram.add(key + NGramAnalyzer.END_DELIMITER);
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

                result += NGramAnalyzer.START_DELIMITER + " " + title + " " + NGramAnalyzer.END_DELIMITER + " ";
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    public static String cleanTitle(String input) {
        String result = "";

        for(String word: input.split("\\s")) {
            word = word.toLowerCase();
            Pattern onlyChars = Pattern.compile("[^A-Za-z0-9]");
            String replaced = onlyChars.matcher(word).replaceAll("");

            if(replaced.length() > 0) {
                result += replaced + " ";

            }
        }

        return trim(result);
    }

    private static HashMap<String, Double> sortByComparator(HashMap<String, Double> unsortMap, final boolean order)
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
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}
