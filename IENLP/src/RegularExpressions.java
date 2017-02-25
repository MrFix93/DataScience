
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Joep on 24-Feb-17.
 */
public class RegularExpressions {
    final static int LIMIT = 20;
    public static void main (String[] args) {
        List<String> tweets = readFile("C:\\Users\\Joep\\Documents\\GitHub\\DataScience\\IENLP\\data\\Wimbledon2014 Tweets.txt"); //load dataset
        List<Data> tweetData = new ArrayList<Data>();
        for(String s : tweets){
            tweetData.add(regex(s)); //for all tweets split into mentions and hastags
        }
        HashMap<String,HashMap<String, Integer>> frequencyTweets = frequency(tweetData); //measure the frequency of metions and hastags over all tweets

        try {
            toCsv("C:\\Users\\Joep\\Documents\\GitHub\\DataScience\\IENLP\\data\\poepje.csv",frequencyTweets); //export list of most occuring tweete and mentions as csv
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
    public static void toCsv(String fileName, HashMap<String,HashMap<String, Integer>> frequencyTweets) throws IOException {
        FileWriter writer = new FileWriter(fileName);
        for(Map.Entry<String, HashMap<String, Integer>> frequencies : frequencyTweets.entrySet()){
            System.out.println(frequencies.getKey());
            CSVUtils.writeLine(writer, Arrays.asList(frequencies.getKey()));
            int i=0;
            for(Map.Entry<String, Integer> map : sortByComparator(frequencies.getValue(),false).entrySet()){
                if(i++ > LIMIT)
                    break;

                System.out.println(map.getKey() + " : " + map.getValue() + " " + i);
                CSVUtils.writeLine(writer, Arrays.asList(map.getKey(), map.getValue().toString()));
            }
            CSVUtils.writeLine(writer, Arrays.asList());
        }
        writer.flush();
        writer.close();
    }
    public static HashMap<String,HashMap<String, Integer>> frequency(List<Data> tweetData){
        HashMap<String, Integer> mapHash  = new HashMap<String, Integer>();
        HashMap<String, Integer> mapMention  = new HashMap<String, Integer>();
        for(Data data : tweetData){
            for(String hashtag : data.hastags){
                if(mapHash.containsKey(hashtag)){
                    mapHash.put(hashtag, mapHash.get(hashtag) + 1);

                }else{
                    mapHash.put(hashtag, 1);
                }
            }
            for(String hashtag : data.mentions){
                if(mapMention.containsKey(hashtag)){
                    mapMention.put(hashtag, mapMention.get(hashtag) + 1);

                }else{
                    mapMention.put(hashtag, 1);
                }
            }
        }
        HashMap<String,HashMap<String, Integer>> myMap  = new HashMap<String,HashMap<String, Integer>>();
        myMap.put("HashTags", mapHash);
        myMap.put("Mentions", mapMention);
        return myMap;
    }

    public static Data regex(String s){
        Pattern hastags = Pattern.compile("\\#(\\w+)");
        Pattern mentions = Pattern.compile("\\@(\\w+)");
        Matcher hastagsM = hastags.matcher(s);
        Matcher mentionsM = mentions.matcher(s);
        List<String> hastagsA = new ArrayList<String>();
        List<String> mentionsA = new ArrayList<String>();
        while(hastagsM.find()) {
            hastagsA.add(hastagsM.group().toLowerCase());
        }
        while(mentionsM.find()) {
            mentionsA.add(mentionsM.group().toLowerCase());
        }
        return new Data(hastagsA,mentionsA);

    }
    public static List<String> readFile(String fileName){
        File file = new File(fileName);
        StringBuilder fileContents = new StringBuilder((int)file.length());
        Scanner scanner;
        List<String> strings = new ArrayList<String>();
        try {
            scanner = new Scanner(file);
            while(scanner.hasNextLine()) {
                strings.add(scanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return strings;

    }
    private static HashMap<String, Integer> sortByComparator(HashMap<String, Integer> unsortMap, final boolean order)
    {

        List<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>()
        {
            public int compare(Map.Entry<String, Integer> o1,
                               Map.Entry<String, Integer> o2)
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
        HashMap<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
