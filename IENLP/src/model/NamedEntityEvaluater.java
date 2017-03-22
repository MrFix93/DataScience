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
    private HashMap<String,ConfusionMatrix> confusionMatrixForTypes;
    private ConfusionMatrix totalConfusionMatrix;

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
    }

    public static void main(String[] args) {

        List<NamedEntityEvaluater> evaluations = new ArrayList<>();

        for(int i = 0; i < args.length; i+=2) {
            NamedEntityEvaluater NEE = new NamedEntityEvaluater(args[i + 1]);
            NEE.start(args[i]);
            evaluations.add(NEE);

            System.out.println(args[i]);
            printConfusionMatrixForTypeTable(NEE.confusionMatrixForTypes);

            double micro_average_precision = microAveragePrecision(NEE.totalConfusionMatrix);
            double micro_average_recall = microAverageRecall(NEE.totalConfusionMatrix);
            double f1_score =  f1Score(micro_average_precision,micro_average_recall);

            System.out.println("micro_average_precision: " + micro_average_precision);
            System.out.println("micro_average_recall: " + micro_average_recall);
            System.out.println("f1_score: " + f1_score);
            System.out.println();

        }

    }

    private void start(String fileName){

        List<String> all_sentences = readFile(fileName);

        totalConfusionMatrix = new ConfusionMatrix();
        confusionMatrixForTypes = new HashMap<>();

        for (String sentence : all_sentences) {

            String [] tabSeperatedSentence= sentence.split("\\t");

            HashMap<String,List<String>> groundTruthEntities = groundTruth(tabSeperatedSentence[1]);
            groundTruthEntities = cleanNotInClassefier(groundTruthEntities);

            HashMap<String,List<String>> foundEntities = sentenceEntities(tabSeperatedSentence[2]);

//                System.out.println("number: "+ tabSeperatedSentence[0] + " sentence {" + tabSeperatedSentence[2]+"}");
//                System.out.println("ground truth:");
//                printEntities(groundTruthEntities);
//                System.out.println("found entities:");
//                printEntities(foundEntities);
//                System.out.println("results confusionMatrix:");

            HashMap<String,ConfusionMatrix> sentenceConfusionMatrixForTypes = calculateSentenceConfusionMatrixForTypes(groundTruthEntities,foundEntities);

            for (Map.Entry<String,ConfusionMatrix> entry : sentenceConfusionMatrixForTypes.entrySet()) {
                String key = entry.getKey();
                ConfusionMatrix typeConfusionMatrix = entry.getValue();

//                System.out.println("key: " + key);
//                typeConfusionMatrix.printMatrix();


                if(confusionMatrixForTypes.containsKey(key)){ //combine matrixes
                    confusionMatrixForTypes.get(key).combine(typeConfusionMatrix);
                }else{ //add new for this type
                    confusionMatrixForTypes.put(key,typeConfusionMatrix);
                }
            }

//            System.out.println("");
        }

        totalConfusionMatrix = allClassConfusionMatrix(confusionMatrixForTypes);
    }

    /**
     * remove all the ground truth entities of classefier classes that are not present in the current used classefier
     * @param entities
     * @return
     */
    private  HashMap<String,List<String>> cleanNotInClassefier(HashMap<String,List<String>>  entities){

        HashMap<String,List<String>> result = new HashMap<>();

        for(Map.Entry<String,List<String>> entry: entities.entrySet()){
            String key = entry.getKey();

            for (NamedEntityClassifier.NamedEntityType namedEntity: classifier.entityTypes) {
                if(namedEntity.TAG.equals(key)){
                    result.put(key,entry.getValue());
                    break;
                }
            }
        }

        return result;
    }

    /**
     * combine all classefiers confusionmatrixes to one total confusion matrix for an classifier
     * @param matrixForTypes
     * @return
     */
    private static ConfusionMatrix allClassConfusionMatrix(HashMap<String,ConfusionMatrix> matrixForTypes){
        ConfusionMatrix result = new ConfusionMatrix();

        for(Map.Entry<String,ConfusionMatrix> entry: matrixForTypes.entrySet()){
            result.combine(entry.getValue());
        }

        return result;
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

    /**
     * calculate the confusion matrix of a sentence using the found entries and the ground truth
     * @param groundTruth
     * @param found
     * @return
     */
    private static HashMap<String,ConfusionMatrix> calculateSentenceConfusionMatrixForTypes(HashMap<String,List<String>> groundTruth, HashMap<String,List<String>> found) {

        HashMap<String,ConfusionMatrix> positiveHalf = calculateHalfSentenceConfusionMatrix(true,found,groundTruth);
        HashMap<String,ConfusionMatrix> negativeHalf = calculateHalfSentenceConfusionMatrix(false,groundTruth,found);

        HashMap<String,ConfusionMatrix> confusionMatrixForTypes = combineHalfSentenceConfusionMatrixes(positiveHalf,negativeHalf);

        return confusionMatrixForTypes;
    }

    /**
     * combine confusionmatrixes for hashmap with classefier classes
     * @param one
     * @param two
     * @return
     */
    private static HashMap<String,ConfusionMatrix> combineHalfSentenceConfusionMatrixes(HashMap<String,ConfusionMatrix> one, HashMap<String,ConfusionMatrix> two){

        HashMap<String,ConfusionMatrix>  result = new HashMap<>();

        for (Map.Entry<String,ConfusionMatrix> entry: one.entrySet()) { //add all one and merge all equal
            String key = entry.getKey();
            ConfusionMatrix tempConfusionMatrix = entry.getValue();

            if(two.containsKey(key)){//merge
                ConfusionMatrix tempConfusionMatrix2 = two.get(key);
                tempConfusionMatrix.combine(tempConfusionMatrix2);

                two.remove(key);
            }

            result.put(key,tempConfusionMatrix);
        }

        result.putAll(two); //add all two that are not equal

        return result;
    }

    /**
     * calculate the confusionmatrix for positive or negative
     * @param positive
     * @param list
     * @param entries
     * @return
     */
    private static HashMap<String,ConfusionMatrix>  calculateHalfSentenceConfusionMatrix(boolean positive, HashMap<String,List<String>> list, HashMap<String,List<String>> entries){

        HashMap<String,ConfusionMatrix>  confusionMatrixForType = new HashMap<>();

        HashMap<String,int[]>  resultsForType = inList(list,entries);

        for (Map.Entry<String,int[]> entry : resultsForType.entrySet()) {

            ConfusionMatrix confusionMatrix = new ConfusionMatrix();

            confusionMatrix.addHalf(positive,entry.getValue()[0],entry.getValue()[1]);
            confusionMatrixForType.put(entry.getKey(),confusionMatrix);
        }

        return confusionMatrixForType;
    }

    /**
     * get for all classefier classes whether strings in list also occur in entries [0] is the amount that are in list and in entries [1] only in list
     * @param list
     * @param entries
     * @return
     */
    private static HashMap<String,int[]>  inList (HashMap<String,List<String>> list, HashMap<String,List<String>> entries){

        HashMap<String,int[]> trueFalseForType = new HashMap<>();

        for (Map.Entry<String,List<String>> listEntry: list.entrySet()) {

            String key = listEntry.getKey();

            int[] trueFalseCount = {0,0};
            trueFalseForType.put(key,trueFalseCount);

            if(entries.containsKey(key)){

                List<String> entriesStrings = entries.get(key);
                List<String> listStrings = listEntry.getValue();

                listLoop:
                for (String listItem : listStrings) {

                    for (String entyItem: entriesStrings){
                        if(listItem.equals(entyItem)){

                            trueFalseForType.get(key)[0] += 1;

                            continue listLoop;
                        }

                    }

                    trueFalseForType.get(key)[1] += 1;
                }
            }else{
                trueFalseForType.get(key)[1] += 1;
            }
        }

        return trueFalseForType;
    }

    /**
     * Get all string belonging to all classifier classes of a sentence
     * @param sentence
     * @return
     */
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

    /**
     * Get strings in a sentence labeled for classifier class(type)
     * @param sentence
     * @param start
     * @param stop
     * @return
     */
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

    /**
     *  Get the ground truth from the string that holds those
     * @param truthString
     * @return
     */
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

    public static void printEntities(HashMap<String,List<String>> list){
        for (Map.Entry<String,List<String>> entry: list.entrySet()) {
            System.out.print("type {" + entry.getKey() +"} ");
            for (String string: entry.getValue()) {
                System.out.print("{" + string + "}");
            }
            System.out.println(";");
        }
    }

    public static void printConfusionMatrixForTypeTable(HashMap<String,ConfusionMatrix> m){

        String format = "%10s%15s%15s%15s%15s";

        System.out.format(format,"type" , "true_positive", "false_positive","true_negative","false_negative");

        for(Map.Entry<String,ConfusionMatrix> e: m.entrySet()){
            ConfusionMatrix confusionMatrix = e.getValue();
            System.out.println();
            System.out.format(format,e.getKey(),confusionMatrix.true_positives,confusionMatrix.false_positives,confusionMatrix.true_negatives,confusionMatrix.false_negatives);

        }
        System.out.println();
    }

    public static void printConfusionMatrixForType(HashMap<String,ConfusionMatrix> m){
        for(Map.Entry<String,ConfusionMatrix> e: m.entrySet()){
            System.out.print("type {" + e.getKey() + "} ");
            e.getValue().printMatrix();
        }
    }

}
