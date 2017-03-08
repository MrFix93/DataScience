package model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static model.Util.cleanTitle;

/**
 * Created by Joep on 08-Mar-17.
 */
public class SentenceModel {

    private String modelSentence;
    private HashMap<String, Double> NGramsProbability;
    /* 3.1 sigir12twiner
    u = 5 is a proper bound as the maximum
    length of a segment, which largely reduces the number of possible
    segmentations.
    **/
    private static int u = 5;
    /* 3.1 sigir12twiner
    We also set e = 5 so that the segmentation only
    focuses on top-quality segments and are not stuck by trivial ones
    **/
    private static int e = 5;

    SentenceModel(HashMap<String, Double> NGramsProbability, String sentence){
        this.modelSentence = modelString(sentence);
        this.NGramsProbability = NGramsProbability;
    }
    public static List<String> createSentenceSegments(String sentence){ //Algorithm 1: Tweet Segmentation sigir12twiner
        List<String[]> segments = new ArrayList<>();
        String[] sentenceWords = sentence.split("\\s");
        for (int i = 1; i < sentenceWords.length; i++){
            String[] tempSegment = Arrays.copyOfRange(sentenceWords,0,i);
            if(i <= u){ //do not split
                segments.add(tempSegment);
            }
            for (int j = 1; j < i ;j++){// try different possible ways to segment
                if(i - j <= u){ //form two shorter segments
                    String splitTempSegment1 = Arrays.copyOfRange(tempSegment,0,j);
                    String splitTempSegment2 = Arrays.copyOfRange(tempSegment,0,j);

                }
            }


        }


        return null;
    }

    public double segmentStickyness(String segment){
        double scp = symmetricalConditionalProbability(segment);
        double stickyness = 2/(1 + Math.pow(Math.E,-scp));//formula (9) sigir12twiner
        return stickyness;
    }
    public double symmetricalConditionalProbability(String segment){
        double sentenceProbability = sentenceProbability(this.NGramsProbability,this.modelSentence);
        String[] splitModelSentence = this.modelSentence.split("\\s");

        double scg = Math.pow(sentenceProbability,2)/; //formula (7) sigir12twiner;
        return 1d;
    }

    public static String modelString(String sentence){
        return LanguageModelMaker.START_DELIMITER + " " + cleanTitle(sentence) + " " + LanguageModelMaker.END_DELIMITER;
    }

    public static double sentenceProbability(HashMap<String, Double> NGramsProbability, String sentence){
        sentence = modelString(sentence);
        String[] sentenceWords = sentence.split("\\s");

        double prob = 0d;
        wordProb :
        for (int wordIndex = 0; wordIndex < sentenceWords.length; wordIndex++){
            String key = sentenceWords[wordIndex];
            for (int n = 1; n < LanguageModelMaker.MAX_SIZE; n++ ){
                if(wordIndex - n < 0) {
                    continue wordProb;
                }

                key = sentenceWords[wordIndex - n] + " " + key;

                //System.out.println("the word is: " + key);
                if(NGramsProbability.containsKey(key)){
                    double wordProb = NGramsProbability.get(key);
                    //System.out.println("the prob of " + key + " is " + wordProb);
                    prob += Math.log10(wordProb);
                }
            }
        }
        //System.out.println("The probability for the sentence " + sentence + " is " + prob);
    return prob;
    }
}
