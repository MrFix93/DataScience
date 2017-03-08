package model;

import java.util.HashMap;

/**
 * Created by Joep on 08-Mar-17.
 */
public class SentenceModel {

    private String sentence;
    private HashMap<String, Double> NGramsProbability;

    SentenceModel(HashMap<String, Double> NGramsProbability, String sentence){
        this.sentence = sentence;
        this.NGramsProbability = NGramsProbability;
    }

    public static double segmentStickyness(String segment){
        double scp = symmetricalConditionalProbability(segment);
        return 2/(1 + Math.pow(Math.E,-scp)); //formula (9) sigir12twiner
    }
    public static double symmetricalConditionalProbability(String segment){

    }
}
