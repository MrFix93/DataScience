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
    static final int  MAX_LINES = 20000;

    /**
     *
     * @param args classifierFolder; {classifierName; typeClass; language}
     */
    public static void main(String[] args){

        String tweetsFile = args[1];
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

        textParser = new TextParser(parserFile);


        List<String> tweets = fileToLineList(tweetFile);

        HashMap<String,Integer> afinn = fileToStringAndValue(afinnFile);


         for (String tweet: tweets) {

            tweet = tweet.replaceAll("[^a-zA-Z0-9 -.,//:]","");






            HashMap<String, List<String>> classification = classifier.classifyString(tweet);


             List<String> allTokens = new ArrayList<>();
             for (Map.Entry<String,List<String>> entry : classification.entrySet()){
                     allTokens.addAll(entry.getValue());
             }

             allTokens = allTokens.stream().map(String::toLowerCase).collect(Collectors.toList()); //to lower case




             if(Collections.disjoint(Arrays.asList(mustWords), allTokens) || !Collections.disjoint(Arrays.asList(notWords), allTokens)){ //if tweet contains  not any of the important words
//                 System.out.println(tweet);
//                 System.out.println("contains no mustWords: " + Collections.disjoint(Arrays.asList(mustWords), allTokens) + " contains notWords: " + !Collections.disjoint(Arrays.asList(notWords), allTokens));
//                 System.out.println();
                 continue;
             }

             List<String> persons = uniquePersons(classification);

            if(persons.size() < 2){
                continue;
            }


            if(!Collections.disjoint(Arrays.asList(drawWords), allTokens) && persons.size() == 2){ //draw thus order does not matter //TODO what if more than two
                printWinnerLoser(tweet,persons.get(0),persons.get(1),true);
                continue;
            }



            //get the last added sentence in List<CoreLabel> format for the parser
            List<CoreLabel> rawClassifiedWords = classifier.rawClassifiedSentences.get(classifier.rawClassifiedSentences.size() - 1);

            Collection<TypedDependency> parsed = textParser.gramStructure(rawClassifiedWords);

            Collection<TypedDependency> parsedPersons = new ArrayList<TypedDependency>();

             String winner = "";
             String loser = "";

            for(TypedDependency p : parsed){

                String v1 = p.dep().originalText();
                String v2 = p.gov().originalText();

                //System.out.println("dep: " + v1 + " gov: " + v2 + " rel: " + p.reln().toString());



                for(String person: persons){
                    if(person.contains(v1)){
                        if(Arrays.asList(winWords).contains(v2)){ //person links to winner
                            winner = v1;
                        }
                        if(Arrays.asList(loseWords).contains(v2)){ //person links to winner
                            loser = v1;
                        }


                    }else if(person.contains(v2)){
                        if(Arrays.asList(winWords).contains(v1)){ //person links to winner
                            winner = v2;
                        }
                        if(Arrays.asList(loseWords).contains(v1)){ //person links to winner
                            loser = v2;
                        }
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

            printWinnerLoser(tweet,winner,loser,false);



            //negativeAndPositiveWords(possible, afinn);

            //TODO add stanford parser





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


    public List<String> uniquePersons(HashMap<String, List<String>> classification){

        List<String> persons = classification.get("PERSON");

        if(persons == null) {
            return new ArrayList<String>();
        }

        Set<String> uniquePersons = new LinkedHashSet<>(persons);

        return new ArrayList<String>(uniquePersons);
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
            int i = MAX_LINES;

            while(scanner.hasNextLine() && i-- > 0) {
                String nextLine = scanner.nextLine();
                //System.out.println(nextLine);
                String line = nextLine.split(";")[2];
                //System.out.println(line);
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
