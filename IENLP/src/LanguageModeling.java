import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Map.Entry;


public class LanguageModeling {
    final static int SIZE = 2;
    final static String END_DELIMITER = "TITLEEND";

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String words = readFile(args[0],1000);

        HashMap<Integer, List<String>> ngrams = createNGrams(words);
        HashMap<Integer, HashMap<String, Integer>> total_count_ngrams = new HashMap<>();

        for(Entry<Integer, List<String>> entry: ngrams.entrySet()) {
            int current_n = entry.getKey();
            HashMap<String, Integer> countNgrams = countNGram(entry.getValue());
            total_count_ngrams.put(current_n, countNgrams);

            for(Entry<String, Integer> ngram: countNgrams.entrySet()) {
                //Double probability = getProbabilityOfNGram(ngram.getValue(), total_count_ngrams.get(current_n - 1));
            }

        }
        System.out.println("finished!");

    }

    public static HashMap<Integer, List<String>> createNGrams(String input){

        String[] words = input.split("\\s");
        System.out.println("amount of words: " + words.length);

        int amountOfWords = words.length;
        HashMap<Integer,List<String>> ngrams = new HashMap<>();

        for(int s = 0; s < SIZE; s++) {
            ngrams.put(s, new ArrayList<>());

            ngram_key_loop:
            for(int i = s;i < words.length; i++) { //iterates over all seperate words
                String key = "";

                for(int w = 0; w < SIZE; w++) { //generate the n-gram classification as key; if 1-gram key is one word, etc
                    String next = words[i - SIZE + w];
                    if(next.equals(LanguageModeling.END_DELIMITER)) {
                        continue ngram_key_loop;
                    }
                    key += next + " ";
                }

                ngrams.get(s).add(key);
            }
        }
        return ngrams;
    }

    public static HashMap<String, Integer> countNGram(List<String> ngrams) {
        HashMap<String, Integer> result = new HashMap<>();
        for(String ngram: ngrams) {
            if(result.containsKey(ngram)) {
                continue;
            }
            int count = Collections.frequency(ngrams, ngram);
            result.put(ngram,count);
        }

        return result;
    }

    public static Double getProbabilityOfNGram(int current_ngram_count, int previous_ngram_count) {
        return Double.valueOf(current_ngram_count/previous_ngram_count);
    }
    /**
     * Reads file, return list of titles associated with a classification (www, SIGDATA etc)
     * @param fileName
     * @param amountOfRows
     * @return
     */
    public static String readFile(String fileName, int amountOfRows){
        String result = "";

        Scanner scanner;
        try {
            scanner = new Scanner(new File(fileName));

            int i = amountOfRows;//1700
            while(scanner.hasNextLine()) {
                if(i-- < 0) {
                    break;
                }
                String[] text = scanner.nextLine().split("\\t");
                String title = text[2].toLowerCase();
                title = title.replaceAll("[/d/.]", "");
                result += title + " " + LanguageModeling.END_DELIMITER + " ";
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }

    private static HashMap<String, Double> sortByComparator(HashMap<String, Double> unsortMap, final boolean order)
    {

        List<Entry<String, Double>> list = new LinkedList<Entry<String, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Double>>()
        {
            public int compare(Entry<String, Double> o1,
                               Entry<String, Double> o2)
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
        for (Entry<String, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

}
