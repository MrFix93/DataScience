package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Pattern;

import static jdk.nashorn.internal.objects.NativeString.trim;


public class NGramAnalyzer {
    final static int MAX_SIZE = 4;
    final static int RESULT_SIZE = 4;
    final static int MAX_RESULTS = 50;
    final static String END_DELIMITER = "TITLEEND";
    final static String START_DELIMITER = "TITLESTART";

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String all_words = readFile(args[0]);

        HashMap<String, Double> NGramsProbability = new HashMap<>();
        List<NGram> ALlNGrams = createNGrams(all_words);

        int[] counts = new int[MAX_SIZE];

        for(NGram ngram: ALlNGrams) {
            for(int i = 0; i < ngram.getLength(); i++) {
                counts[i]++;
            }
        }

        for(NGram ngram: ALlNGrams) {
            // Calculate the P(n|n-1) for every NGrams and add to the list
            NGramsProbability.putAll(ngram.getAllP(all_words, counts));
        }

        HashMap<String, Double> sorted = sortByComparator(NGramsProbability,false);

        int count = 0;
        String[] result = new String[MAX_SIZE];
        HashMap<Integer, List<String>> results = new HashMap<>();

        for(int i = 2; i <= MAX_SIZE; i++) {
            results.put(i, new ArrayList<>());
        }

        for(Map.Entry<String, Double> entry: sorted.entrySet()) {
            int n = entry.getKey().split("\\s").length;

            int current_length = results.get(n).size();
            if(current_length <= MAX_RESULTS) {
                results.get(n).add("(" + current_length + ") " + entry.getKey() + ": " + entry.getValue());
            }

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

        for(int i = 2; i <= MAX_SIZE; i++) {
            System.out.println("TOP " + MAX_RESULTS + " of " + i + "-gram");
            for(String print: results.get(i)){
                System.out.println(print);
            }
            System.out.println("END TOP " + MAX_RESULTS + " of " + i + "-gram");
        }

        System.out.println("finished!");
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

            NGram newGram = new NGram();
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

            while(scanner.hasNextLine()) {

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
