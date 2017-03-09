package model;

import java.util.*;
import java.util.regex.Pattern;

import static jdk.nashorn.internal.objects.NativeString.trim;

/**
 * Created by peterwessels on 01/03/2017.
 */
public class Util {
    public static int countSubstring(String subStr, String str){
        return (str.length() - str.replace(subStr, "").length()) / subStr.length();
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

    public static HashMap<String, Double> sortByComparator(HashMap<String, Double> unsortMap, final boolean order)
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

    public static void log(String type, String message) {
        System.out.println("[" + type + "] " + message);
    }


    public static  String[] combine(String[] a, String[] b){
        int length = a.length + b.length;
        String[] result = new  String[length];
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }


    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )
    {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o1.getValue()).compareTo( o2.getValue() );
            }
        } );

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }
}
