package model;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TypedDependency;
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
    TextParser textParser;

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

    public void start(String tweetFile,String afinnFile,String parserFile){

        textParser = new TextParser(parserFile);


        List<String> tweets = fileToLineList(tweetFile);

        HashMap<String,Integer> afinn = fileToStringAndValue(afinnFile);

        for (String tweet: tweets) {

            HashMap<String, List<String>> classification = classifier.classifyString(tweet);

            String possible = stringWithTwoOrMoreUniquePersons(classification,tweet);


            if(possible == null){
                continue;
            }

            List<String> persons = classification.get("PERSON"); //TODO remove dublicate

            //get the last added sentence in List<CoreLabel> format for the parser
            List<CoreLabel> rawClassifiedWords = classifier.rawClassifiedSentences.get(classifier.rawClassifiedSentences.size() - 1);

            Collection<TypedDependency> parsed = textParser.gramStructure(rawClassifiedWords);

            for(TypedDependency p : parsed){
                System.out.println(p.dep().originalText() + " " + p.gov().originalText() + " " + p.reln());
            }

//            String[] relation = relation(parsed,0,persons,null,null);
//
//            if(relation != null) {
//                System.out.println("p1: " + relation[0] + " p2: " + relation[2]);
//            }





            negativeAndPositiveWords(possible, afinn);

            //TODO add stanford parser





        }


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
            System.out.println(value + " " + index);
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

    static final int  MAX_LINES = 200;
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
