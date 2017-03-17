package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.*;

import static model.NamedEntityEvaluater.groundTruth;
import static model.Util.cleanTitle;

/**
 * Created by Joep on 17-Mar-17.
 */
public class NamedEntityEvaluater {
    private List<String>  all_sentences;
    final static int AMOUNT_RECORDS = 100;

    final static String TAG_ORGANIZATION = "ORG";
    final static String START_TAG_ORGANIZATION = "<ORGANIZATION>";
    final static String STOP_TAG_ORGANIZATION = "</ORGANIZATION>";

    final static String TAG_LOCATION  = "LOC";
    final static String START_TAG_LOCATION = "<LOCATION>";
    final static String STOP_TAG_LOCATION = "</LOCATION>";

    final static String TAG_PERSON  = "PER";
    final static String START_TAG_PERSON = "<PERSON>";
    final static String STOP_TAG_PERSON = "</PERSON>";

    public int true_positives = 0;
    public int false_positives = 0;
    public int false_negatives = 0;
    public int true_negatives = 0;

    public NamedEntityEvaluater(){}

    public static void main(String[] args) {
        NamedEntityEvaluater NEE = new NamedEntityEvaluater();
        NEE.start(args[0]);
    }

    public void start(String fileName){
        all_sentences = readFile(fileName);

        for (String sentence : all_sentences) {
            String [] tabSeperatedSentence= sentence.split("\\t");
            HashMap<String,List<String>> groundTruthEntities = groundTruth(tabSeperatedSentence[1]);
            HashMap<String,List<String>> foundEntities = sentenceEntities(tabSeperatedSentence[2]);
        }
    }

    public static HashMap<String,List<String>> sentenceEntities(String sentence){
        HashMap<String,List<String>> result = new HashMap<>();

        List<String> words = Arrays.asList(sentence.split("\\s"));

        List<String> organisationEntities = sentenceEntitiesForType(words,START_TAG_ORGANIZATION,STOP_TAG_ORGANIZATION);
        result.put(TAG_ORGANIZATION,organisationEntities);

        List<String> locationEntities = sentenceEntitiesForType(words,START_TAG_LOCATION,STOP_TAG_LOCATION);
        result.put(TAG_LOCATION,organisationEntities);

        List<String> personEntities = sentenceEntitiesForType(words,START_TAG_PERSON,STOP_TAG_PERSON);
        result.put(TAG_PERSON,organisationEntities);

        return result;
    }

    public static List<String> sentenceEntitiesForType(List<String> words, String start, String stop){
        List<String> result = new ArrayList<>();

        while(words.indexOf(start) != -1) {

            int startEntity = words.indexOf(start);
            int stopEntity = words.indexOf(stop);

            List<String> stringList = words.subList(startEntity + 1, stopEntity - 1);
            String string = String.join(" ", stringList);

            result.add(string);
            words.subList(stopEntity,words.size() - 1);
        }

        return result;
    }

    public static HashMap<String,List<String>> groundTruth(String truthString){
        System.out.println("String {" + truthString + "}");
        HashMap<String,List<String>> result = new HashMap<>();

        String[] namedEntities = truthString.split(";");

        for (String entity: namedEntities) {
            System.out.println("entity {" + entity + "}");
            String[] typeAndString =  entity.split("/");
            System.out.println("length type and string " + typeAndString.toString());
            String type = typeAndString[0];
            String string = typeAndString[1];

            if(result.containsKey(type)){ //add to the list
                result.get(type).add(string);
            }else{
                List<String> strings = new ArrayList<String>(Arrays.asList(string));
                result.put(type,strings);
            }

        }

        return result;
    }





    public static List<String>  readFile(String fileName){
        List<String> results = new ArrayList<>();

        Scanner scanner;
        try {
            scanner = new Scanner(new File(fileName));
            int records = AMOUNT_RECORDS;
            while(scanner.hasNextLine()) {
                if(records-- < 0) {
                    break;
                }

                //String[] text = scanner.nextLine().split("\\t");
                results.add(scanner.nextLine());
//                String title = text[2];
//                title = cleanTitle(title);
//
//                result += title;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return results;
    }
}
