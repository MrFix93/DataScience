package model;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import model.NamedEntityClassifiers.NamedEntityClassifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by Joep on 22-Mar-17.
 */
public class RelationExtraction {

    List<NamedEntityClassifier> classifiers;
    NamedEntityClassifier classifier;
    TextParser textParser;
    static final int  MAX_LINES = 3000000;

    /**
     *
     * @param args classifierFolder; {classifierName; typeClass; language}
     *
    englishPCFG.ser.gz
    C:\Users\Joep\Documents\Wimbledon2014Tweets.txt
    C:\Users\Joep\Documents\DataScience\IENLP\classifiers\
    C:\Users\Joep\Documents\DataScience\IENLP\data\AFINN-111.txt
    english.all.3class.distsim.crf.ser.gz
    3class
    english
     */
    public static void main(String[] args){

        String tweetsFile = args[1];
        System.out.println(tweetsFile);
        String classifierFolder = args[2];
        String affinFile = args[3];
        String parserFile = args[2] + args[0];

        String[] classifierArgs = Arrays.copyOfRange(args, 4,args.length);

        List<NamedEntityClassifier> namedEntityClassifierList = classifiersFromArgs(classifierFolder,classifierArgs);

        RelationExtraction relationExtraction = new RelationExtraction(namedEntityClassifierList);


        relationExtraction.start(tweetsFile,affinFile,parserFile);

    }

    public RelationExtraction(List<NamedEntityClassifier> classifiers){
        this.classifiers = classifiers;
        classifier = classifiers.get(0); //todo not hardcode
    }

    final static String[] mustWords = {"wimbledon","#wimbledon2014", "#sport","wimbledon2014","#tennis"};
    final static String[] notWords = {"pre-wimbledon","could","would","should","practice","practising","prepares","preperation"};
    final static String[] drawWords = {"draw"};
    final static String[] winWords = {"wins","claims","claim","beat","beats","defeats","achieves","scores","succes"};
    final static String[] loseWords = {"lose","loses","behind","defeat","fails","failure"};

    public void start(String tweetFile,String afinnFile,String parserFile){
        List<String> mustWordsList = Arrays.asList(mustWords);
        List<String> notWordsList = Arrays.asList(notWords);
        List<String> drawWordsList = Arrays.asList(drawWords);
        List<String> winWordsList = Arrays.asList(winWords);
        List<String> loseWordsList = Arrays.asList(loseWords);

        Set<String> result = new HashSet<>();

        textParser = new TextParser(parserFile);

        Set<String> tweets = fileToLineList(tweetFile);
        int amountTweets = tweets.size();

        int index = 0;
         for (String tweet: tweets) {
            index++;
            System.out.println(index + " of " + amountTweets + " tweets relation extracted");

            tweet = tweet.replaceAll("[^a-zA-Z0-9 -.,//:]","");

            HashMap<String, List<String>> classification = classifier.classifyString(tweet);


             Collection<List<String>> allTokensMulti = new ArrayList<>(classification.values());
             List<String> allTokens = new ArrayList<>();
             for (List<String> string : allTokensMulti){
                     allTokens.addAll(string);
             }

             allTokens = allTokens.stream().map(String::toLowerCase).collect(Collectors.toList()); //to lower case


             if(Collections.disjoint(mustWordsList, allTokens) || !Collections.disjoint(notWordsList, allTokens)){ //if tweet contains  not any of the important words
                 continue;
             }

             List<String> persons = uniquePersons(classification);

            if(persons.size() < 2){
                continue;
            }


            if(!Collections.disjoint(drawWordsList, allTokens) /*&& persons.size() == 2 **/){ //draw thus order does not matter //TODO what if more than two
                continue;
            }



            //get the last added sentence in List<CoreLabel> format for the parser
            List<CoreLabel> rawClassifiedWords = classifier.rawClassifiedSentences.get(classifier.rawClassifiedSentences.size() - 1);

            Collection<TypedDependency> parsed = textParser.gramStructure(rawClassifiedWords);


             String winner = "";
             String loser = "";

            for(TypedDependency p : parsed){

                String v1 = p.dep().originalText().toLowerCase();
                String v2 = p.gov().originalText().toLowerCase();

                for(String person: persons){
                    if(person.contains(v1)){
                        if(winWordsList.contains(v2)){ //person links to winner
                            winner = v1;
                        }
                        if(loseWordsList.contains(v2)){ //person links to winner
                            loser = v1;
                        }


                    }else if(person.contains(v2)){
                        if(winWordsList.contains(v1)){ //person links to winner
                            winner = v2;
                        }
                        if(loseWordsList.contains(v1)){ //person links to winner
                            loser = v2;
                        }
                    }
                    if(!loser.isEmpty() && !winner.isEmpty()){
                        break;
                    }
                }
            }

            if(loser.isEmpty() && !winner.isEmpty()){
                for(String person : persons){
                    if(!person.equals(winner)){
                        loser = person;
                        continue;
                    }
                }
            }else if(!loser.isEmpty() && winner.isEmpty()){
                for(String person : persons){
                    if(!person.equals(loser)){
                        winner = person;
                        continue;
                    }
                }
            }

            //printWinnerLoser(tweet,winner,loser,false);
            result.add("Defeat( " + winner + " , " + loser + " )");

            System.out.println("intermidiate results:");
            printResult(result);
        }
        System.out.println("finished");
         printResult(result);


    }

    public void printResult(Set<String> result){
        for(String string : result){
            System.out.println(string);
        }
    }
    public static void printWinnerLoser(String tweet,String winner, String loser, boolean draw){

        if(winner.isEmpty() || loser.isEmpty()){
            //System.out.println("no winner or loser: " + tweet);
            return;
        }

        System.out.println(tweet);
        if(draw){
            System.out.println("Result( draw: " + winner +" , draw: " + loser + " );");
        }else {
            System.out.println("Result( winner: " + winner + " , loser: " + loser + " );");
        }
        System.out.println();
    }

//Does not work like this see https://www.google.nl/url?sa=i&rct=j&q=&esrc=s&source=images&cd=&cad=rja&uact=8&ved=0ahUKEwjpmdanq-3SAhVEOhQKHb1wBUIQjRwIBw&url=http%3A%2F%2Falfa-img.com%2Fshow%2Fsyntax-sentence-tree.html&psig=AFQjCNEHbd8MhaUgIfahoUfvHVmO7qHSbQ&ust=1490382933651654
    public static String[] relation(Tree parsed, int index,List<String> persons,String p1, String p2){
        index *= 10;
        List<Tree> children = parsed.getChildrenAsList();
        if(children.size() == 0)
            return null;
        for(Tree child : children){
            index += 1;
            String value = child.value();
            //System.out.println(value + " " + index);
            for (String person : persons){
                if(person.contains(value)) {
                    if(p1 == null) {
                        p1 = value;
                    }else if(value != p1){
                        String[] ar = {p1,p2};
                        return ar;
                    }
                }
            }
            //System.out.println("node: " + child.value() + " index: " + index);
            relation(child,index,persons, p1, p2);
        }

        return null;
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
            //System.out.println(string);
            for (Map.Entry<String, Integer> entry : afinnWords.entrySet()) {
                //System.out.println("afinn words: " + entry.getKey() + " " + entry.getValue());
            }
        }
    }

    public static void printList(List<String> list){
        for(String string : list){
            System.out.println(string);
        }
    }


    public List<String> uniquePersons(HashMap<String, List<String>> classification){

        List<String> persons = classification.get("PERSON");

        if(persons == null) {
            return new ArrayList<>();
        }

        List<String> uniquePersonsTemp = new ArrayList<>(new LinkedHashSet<>(persons));
//        System.out.println("temp list");
//        printList(persons);


        List<String> uniquePersons = new ArrayList<>();

        uniquePersons :for(String personTemp : uniquePersonsTemp){
            if(personTemp.length() < 2){ //if is not a name
                continue ;
            }
            personTemp = personTemp.toLowerCase();


            for (String person : uniquePersons){
                person = person.toLowerCase();

                if(person.contains(personTemp) || personTemp.contains(person)){
                    System.out.println("is in list" + person + " by " + personTemp);

                    if(person.length() < personTemp.length()){ //replace if the name is longer
                        System.out.println("replace" + person + " by " + personTemp);
                        uniquePersons.remove(person);
                        uniquePersons.add(personTemp);
                        //uniquePersons.set(uniquePersons.indexOf(person),personTemp);
                    }

                    continue uniquePersons;
                }
            }
            uniquePersons.add(personTemp);
        }
//        System.out.println("final list");
//        printList(uniquePersons);

        return uniquePersons;
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


    public static Set<String>  fileToLineList(String fileName){
        Set<String> results = new HashSet<>();

        Scanner scanner = null;

        try {

            scanner = new Scanner(new File(fileName));
            int i = 0;

            while(scanner.hasNextLine() && i++ < MAX_LINES) {
                System.out.println(i + " of " + MAX_LINES + " lines read of file");
                String nextLine = scanner.nextLine();
                //System.out.println(nextLine);
                String[] line = nextLine.split(";");
                if(line.length > 1) {
                    //System.out.println(line);
                    results.add(line[2]);
                }

            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        scanner.close();
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
