import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Scanner;


public class LanguageModeling {
    final static int SIZE = 4;

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        List<String> words = readFile("C:\\Users\\Joep\\Documents\\GitHub\\DataScience\\IENLP\\data\\DBLPTrainset(1).txt",1000);
        HashMap<String,Double> wordsProbability =  wordProbability(words);
        HashMap<String,Double> wordsProbabilitySorted = sortByComparator(wordsProbability,false);

        double singleOccurance =  1d / Double.valueOf(words.size());
        for(Entry<String, Double> word :wordsProbabilitySorted.entrySet()){
            if(word.getValue() > singleOccurance)
                System.out.println(word.getKey() + " " + word.getValue());
            else
                break;
        }
        System.out.println("finished!");

    }
    public static HashMap<String,Double> wordProbability(List<String> words){
        HashMap<String,Double> wordCount = new HashMap<String,Double>();
        System.out.println("amount of words: " + words.size());
        int amountOfWords = words.size();
        for(int i = SIZE;i < words.size(); i++){
            String key = "";
            for(int w = 0; w < SIZE; w++){
                key += words.get(i - SIZE + w) + " ";
            }
            if(wordCount.containsKey(key)) //if key is already present in list dont calculate again
                continue;

            int count = 0;
            for(int q = SIZE;q < words.size(); q++){
                if(q == i) //don't get self
                    continue;
                boolean match = true;
                for(int w = 0; w < SIZE; w++){
                    if(!words.get(i - SIZE + w).equals(words.get(q - SIZE + w))){
                        match = false;
                        break;
                    }
                }
                if(match){ //if the combination is equal
                    count++;
                }
            }
            double prob = Double.valueOf(count + 1) / Double.valueOf(amountOfWords);
            wordCount.put(key, prob);
        }
        return wordCount;
    }
    public static List<String> readFile(String fileName,int amountOfRows){
        File file = new File(fileName);
        List<String> words = new ArrayList<String>();
        String text = "";
        Scanner scanner;
        try {
            scanner = new Scanner(file);

            int i = amountOfRows;//1700
            while(scanner.hasNextLine()) {
                if(i-- < 0)
                    break;
                text += scanner.nextLine().toLowerCase();
            }
            words = new ArrayList<String>(Arrays.asList(text.split("\\W")));
            Iterator<String> it = words.iterator();
            while (it.hasNext()) {
                String name = it.next();
                if(name.equals("")){
                    it.remove();
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
