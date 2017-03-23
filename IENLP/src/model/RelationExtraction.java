package model;

import model.NamedEntityClassifiers.NamedEntityClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;



/**
 * Created by Joep on 22-Mar-17.
 */
public class RelationExtraction {

    List<NamedEntityClassifier> classifiers;
    NamedEntityClassifier classifier;

    /**
     *
     * @param args classifierFolder; {classifierName; typeClass; language}
     */
    public static void main(String[] args){

        String tweetsFile = args[0];
        String classifierFolder = args[1];
        String affinFile = args[2];

        String[] classifierArgs = Arrays.copyOfRange(args, 3,args.length);

        List<NamedEntityClassifier> namedEntityClassifierList = classifiersFromArgs(classifierFolder,classifierArgs);

        RelationExtraction relationExtraction = new RelationExtraction(namedEntityClassifierList);

        relationExtraction.start(tweetsFile,affinFile);

    }

    public RelationExtraction(List<NamedEntityClassifier> classifiers){
        this.classifiers = classifiers;
        classifier = classifiers.get(0); //todo not hardcode
    }

    public void start(String tweetFile,String afinnFile){


        List<String> tweets = fileToLineList(tweetFile);

        HashMap<String,Integer> afinn = fileToStringAndValue(afinnFile);

        for (String tweet: tweets) {

            HashMap<String, List<String>> classification = classifier.classifyString(tweet);

            String possible = stringWithTwoOrMoreUniquePersons(classification,tweet);


            if(possible == null){
                continue;
            }

            negativeAndPositiveWords(possible, afinn);

            //TODO add stanford parser





        }


    }

    public static void negativeAndPositiveWords(String string, HashMap<String,Integer> afinn){
        String[] words = string.split("\\s");

        HashMap<String,Integer> afinnWords = new HashMap<>();

        for (String word : words){
            for(Map.Entry<String,Integer> afinnEntry : afinn.entrySet()){

                String key = afinnEntry.getKey();
                if(word.equals(key)){ //word is in list
                    afinnWords.put(key,afinnEntry.getValue());
                }

            }
        }

        if(afinnWords.size() > 0) {
            System.out.println(string);
            for (Map.Entry<String, Integer> entry : afinnWords.entrySet()) {
                System.out.println("afinn words: " + entry.getKey() + " " + entry.getValue());
            }
        }
    }


    public String stringWithTwoOrMoreUniquePersons(HashMap<String, List<String>> classification, String string){

        List<String> persons = classification.get("PERSON");

        if(persons != null) {
            Set<String> uniquePersons = new LinkedHashSet<>(persons);

            if (uniquePersons.size() > 1) {
                return string;

            }

        }

        return null;
    }


    public static List<NamedEntityClassifier> classifiersFromArgs(String classifierFolder,String[] args){

        List<NamedEntityClassifier> result = new ArrayList<>();

        for(int i = 0; i < args.length; i += 3){

            NamedEntityClassifier namedEntityClassifier =  createClassifier(classifierFolder,args[0],args[1],args[2]);
            result.add(namedEntityClassifier);

        }

        return result;
    }

    public static NamedEntityClassifier createClassifier(String classifierFolder, String classifierName, String typeClass, String language){

        NamedEntityClassifier namedEntityClassifier = NamedEntityClassifier.getClassifierForTypeClass(typeClass);
        namedEntityClassifier.setClassifier(classifierFolder,classifierName);
        namedEntityClassifier.language = language;

        return namedEntityClassifier;
    }

    public static List<String>  fileToLineList(String fileName){
        List<String> results = new ArrayList<>();

        Scanner scanner;

        try {

            scanner = new Scanner(new File(fileName));

            while(scanner.hasNextLine()) {
                String line = scanner.nextLine().split("\\t")[2];
                results.add(line);

            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return results;
    }

    public static HashMap<String,Integer>  fileToStringAndValue(String fileName){
        HashMap<String,Integer> result = new HashMap<>();

        Scanner scanner;

        try {

            scanner = new Scanner(new File(fileName));

            while(scanner.hasNextLine()) {
                String[] line = scanner.nextLine().split("\\t");
                result.put(line[0],Integer.parseInt(line[1]));

            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return result;
    }


}
