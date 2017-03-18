package model;

import model.NamedEntityClassifiers.NamedEntityClassifier;
import model.NamedEntityClassifiers.NamedEntityClassifier_3class;
import model.NamedEntityClassifiers.NamedEntityClassifier_4class;
import model.NamedEntityClassifiers.NamedEntityClassifier_7class;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Created by Joep on 17-Mar-17.
 */
public class NamedEntityEvaluater {

    private NamedEntityClassifier classifier;
    private ConfusionMatrix confusionMatrix;

    private NamedEntityEvaluater(String classClassifier){

        switch (classClassifier) {
            case NamedEntityClassifier_3class.typeClass:
                this.classifier = new NamedEntityClassifier_3class();

                break;
            case NamedEntityClassifier_4class.typeClass:
                this.classifier = new NamedEntityClassifier_4class();

                break;
            case NamedEntityClassifier_7class.typeClass:
                this.classifier = new NamedEntityClassifier_7class();

                break;
            default:
                this.classifier = new NamedEntityClassifier_3class();

                break;
        }

        this.confusionMatrix = new ConfusionMatrix();
    }

    public static void main(String[] args) {

        List<NamedEntityEvaluater> evaluations = new ArrayList<>();

        for(int i = 0; i < args.length; i+=2) {
            NamedEntityEvaluater NEE = new NamedEntityEvaluater(args[i + 1]);
            NEE.start(args[i]);
            evaluations.add(NEE);

            System.out.println(args[i]);
            System.out.println("true_positives: " + NEE.confusionMatrix.true_positives + "; false_positives: " + NEE.confusionMatrix.false_positives + "; true_negatives: " + NEE.confusionMatrix.true_negatives + "; false_negatives: " + NEE.confusionMatrix.false_negatives);
            System.out.println();
        }

        ConfusionMatrix totalConfusionMatrix = totalConfusionMatrix(evaluations);
        double micro_average_precision = microAveragePrecision(totalConfusionMatrix);
        double micro_average_recall = microAverageRecall(totalConfusionMatrix);
        double f1_score =  f1Score(micro_average_precision,micro_average_recall);

        System.out.println("micro_average_precision: " + micro_average_precision);
        System.out.println("micro_average_recall: " + micro_average_recall);
        System.out.println("f1_score: " + f1_score);

    }

    private void start(String fileName){
        List<String> all_sentences = readFile(fileName);

        for (String sentence : all_sentences) {

            String [] tabSeperatedSentence= sentence.split("\\t");

            HashMap<String,List<String>> groundTruthEntities = groundTruth(tabSeperatedSentence[1]);
            HashMap<String,List<String>> foundEntities = sentenceEntities(tabSeperatedSentence[2]);

            ConfusionMatrix sentenceConfusionMatrix = calculateSentenceConfusionMatrix(groundTruthEntities,foundEntities);
            confusionMatrix.combine(sentenceConfusionMatrix); //add to total

//            System.out.println("number: "+ tabSeperatedSentence[0] + " sentence {" + tabSeperatedSentence[2]+"}");
//            System.out.println("ground truth:");
//            printEntities(groundTruthEntities);
//            System.out.println("found entities:");
//            printEntities(foundEntities);
//            System.out.println("true_positives: " + sentenceConfusionMatrix.true_positives + "; false_positives: " + sentenceConfusionMatrix.false_positives + "; true_negatives: " + sentenceConfusionMatrix.true_negatives + "; false_negatives: " + sentenceConfusionMatrix.false_negatives);
//            System.out.println("");

        }
    }

    private static ConfusionMatrix totalConfusionMatrix(List<NamedEntityEvaluater> evaluations){
        ConfusionMatrix totalConfusionMatrix = new ConfusionMatrix();

        for (NamedEntityEvaluater evaluation : evaluations) {
            totalConfusionMatrix.combine(evaluation.confusionMatrix);
        }

        return totalConfusionMatrix;
    }

    private static double f1Score(double precision,double recall){
        return 2d * ((precision * recall) / (precision + recall));
    }

    private static double microAveragePrecision(ConfusionMatrix totalConfusionMatrix){
        return (double)totalConfusionMatrix.true_positives / (double)(totalConfusionMatrix.true_positives + totalConfusionMatrix.false_positives);
    }

    private static double microAverageRecall(ConfusionMatrix totalConfusionMatrix){

        return (double)totalConfusionMatrix.true_positives / (double)(totalConfusionMatrix.true_positives + totalConfusionMatrix.false_negatives);
    }

    private static ConfusionMatrix calculateSentenceConfusionMatrix(HashMap<String,List<String>> groundTruth, HashMap<String,List<String>> found) {
        ConfusionMatrix sentenceConfusionMatrix = new ConfusionMatrix();

        int[] positives = inList(found,groundTruth);
        sentenceConfusionMatrix.true_positives += positives[0];
        sentenceConfusionMatrix.false_positives += positives[1];

        int[] negatives = inList(groundTruth,found);
        sentenceConfusionMatrix.true_negatives += negatives[0]; //TODO not sure how to calculate this!
        sentenceConfusionMatrix.false_negatives += negatives[1];

        return sentenceConfusionMatrix;

    }

    private static int[] inList (HashMap<String,List<String>> list, HashMap<String,List<String>> entries){

        int trueV = 0;
        int falseV = 0;

        for (Map.Entry<String,List<String>> listEntry: list.entrySet()) {
            String key = listEntry.getKey();

            if(entries.containsKey(key)){

                List<String> entriesStrings = entries.get(key);
                List<String> listStrings = listEntry.getValue();

                listLoop:
                for (String listItem : listStrings) {

                    for (String entyItem: entriesStrings){
                        if(listItem.equals(entyItem)){
                            trueV += 1;
                            continue listLoop;
                        }

                    }

                    falseV += 1;

                }
            }else{
                falseV += 1;
            }
        }

        int[] result = {trueV,falseV};
        return result;
    }

    private static void printEntities(HashMap<String,List<String>> list){
        for (Map.Entry<String,List<String>> entry: list.entrySet()) {
            System.out.print("type {" + entry.getKey() +"} ");
            for (String string: entry.getValue()) {
                System.out.print("{" + string + "}");
            }
            System.out.println(";");
        }
    }

    private  HashMap<String,List<String>> sentenceEntities(String sentence){

        HashMap<String,List<String>> result = new HashMap<>();

        for (NamedEntityClassifier.NamedEntityType namedEntity: classifier.entityTypes) {

            List<String> entityStrings = sentenceEntitiesForType(sentence,namedEntity.START,namedEntity.STOP);
            if(entityStrings.size() > 0) {
                result.put(namedEntity.TAG, entityStrings);
            }

        }

        return result;
    }

    private static List<String> sentenceEntitiesForType(String sentence, String start, String stop){

        List<String> result = new ArrayList<>();

        while(sentence.contains(start) || sentence.contains(stop)) {

            int startEntity = sentence.indexOf(start);
            int stopEntity = sentence.indexOf(stop);
            int cutOff = stopEntity + stop.length();

            if(stopEntity == -1){ //close tag is not placed if it is at the end of the sentence
                //System.out.println("last tag is missing");
                stopEntity = sentence.length() - 1;
                cutOff = sentence.length();
            }
            if(startEntity == -1 || startEntity > stopEntity){
                //System.out.println("first tag is missing");
                startEntity = -start.length();
            }
            String string = sentence.substring(startEntity + start.length(), stopEntity);

            result.add(string);

            sentence = sentence.substring(cutOff);
        }

        return result;
    }

    private static HashMap<String,List<String>> groundTruth(String truthString){
        HashMap<String,List<String>> result = new HashMap<>();

        if(!truthString.isEmpty()) {

            String[] namedEntities = truthString.split(";");

            for (String entity : namedEntities) {

                String[] typeAndString = entity.split("/");
                String type = typeAndString[0];
                String string = typeAndString[1];

                if (result.containsKey(type)) { //add to the list
                    result.get(type).add(string);
                } else {
                    List<String> strings = new ArrayList<>(Arrays.asList(string));
                    result.put(type, strings);
                }

            }

        }
        return result;
    }

    private static List<String>  readFile(String fileName){
        List<String> results = new ArrayList<>();

        Scanner scanner;

        try {

            scanner = new Scanner(new File(fileName));

            while(scanner.hasNextLine()) {

                results.add(scanner.nextLine());

            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return results;
    }
}
