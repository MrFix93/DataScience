package model;

import com.sun.deploy.util.StringUtils;
import org.apache.lucene.analysis.LetterTokenizer;
import org.apache.lucene.analysis.TokenStream;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class LanguageModeling {
    final static int SIZE = 4;
    final static String SEPARATOR = "TITLE_END";

    public static void main(String[] args) {

        for(Entry<String, List<String>> titleSet:
                readFile(args[0],1000).entrySet()) {

            System.out.println("Starting with classification " + titleSet.getKey());
            ArrayList<String> ngrams = new ArrayList<>();

            // Add all titles to a list, including the seperator after each title

            for(String title : titleSet.getValue()) {

                ngrams.addAll(
                        NGramAnalyzer.createNGramList(
                                NGramAnalyzer.tokenizeString(title)
                        )
                );

                /*
                total_text.addAll(
                        Arrays.asList(
                                title.split("\\s")));
                total_text.add(SEPARATOR);
                raw_text += title;
                */
            }

            System.out.println(ngrams);
            /*
            // Check for each n-gram the occurence in the total_text
            for(String ngram: ngrams) {
                if (!ngramPropability.containsKey(ngram)) {
                    ngramPropability.put(ngram, getNGramPropability(ngram, raw_text, ngrams.size()));
                }
            }

            System.out.println(ngramPropability);
            */
        }

        /*
        double singleOccurance =  1d / Double.valueOf(words.size());
        for(Entry<String, Double> word :wordsProbabilitySorted.entrySet()){
            if(word.getValue() > singleOccurance)
                System.out.println(word.getKey() + " " + word.getValue());
            else
                break;
        }
        */
        System.out.println("finished!");

    }

    public static double getNGramPropability(String ngram, String total_text, int total_ngrams) {
        return countNGram(ngram, total_text) / total_ngrams;
    }

    public static String getSubString(int start, int end, ArrayList<String> words) throws Exception {
        int _start = 0;
        int _end = 0;
        if(start > end) {
            _start = end;
            _end = start;
        }

        List<String> toReturn = words.subList(_start, _end);

        if(toReturn.contains(SEPARATOR)) {
            throw new Exception("Separator found in n-gram");
        }

        return StringUtils.join(toReturn, " ");
    }

    public static int countNGram(String search, String total_text) {
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){

            lastIndex = total_text.indexOf(search,lastIndex);

            if(lastIndex != -1){
                count ++;
                lastIndex += search.length();
            }
        }

        return count;
    }

    /**
     * Reads file, return list of titles associated with a classification (www, SIGDATA etc)
     * @param fileName
     * @param amountOfRows
     * @return
     */
    public static Map<String, List<String>> readFile(String fileName,int amountOfRows){
        Map<String, List<String>> words = new HashMap<>();

        Scanner scanner;
        try {
            scanner = new Scanner(new File(fileName));

            int i = amountOfRows;//1700
            while(scanner.hasNextLine()) {
                if(i-- < 0)
                    break;
                String[] text = scanner.nextLine().split("\\t");

                if(words.containsKey(text[1])) {
                    words.get(text[1]).add(text[2]);
                } else {
                    words.put(text[1], new ArrayList<>(Arrays.asList(text[2])));
                }

            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return words;
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
