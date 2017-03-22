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
        String[] classifierArgs = Arrays.copyOfRange(args, 2,args.length);

        List<NamedEntityClassifier> namedEntityClassifierList = classifiersFromArgs(classifierFolder,classifierArgs);

        RelationExtraction relationExtraction = new RelationExtraction(namedEntityClassifierList);

        relationExtraction.start(tweetsFile);

    }

    public RelationExtraction(List<NamedEntityClassifier> classifiers){
        this.classifiers = classifiers;
        classifier = classifiers.get(0); //todo not hardcode
    }

    public void start(String tweetFile){


        List<String> tweets = fileToLineList(tweetFile);

        for (String tweet: tweets) {

            HashMap<String, List<String>> classification = classifier.classifyString(tweet);

            String possible = stringWithTwoOrMoreUniquePersons(classification,tweet);

            if(possible != null){
                System.out.println(possible);
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


}
