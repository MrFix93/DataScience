package model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static model.Util.cleanTitle;

/**
 * Created by peterwessels on 06/03/2017.
 */
public class LanguageModel {
    Map<String, Double> NGrams = new HashMap<>();
    BufferedReader in;


    public static void main(String[] args) {
        LanguageModel lm = new LanguageModel();
        lm.in = new BufferedReader(new InputStreamReader(System.in));
        lm.start();
    }

    public void start() {
        try {
            String input = this.in.readLine();


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sentenceProbability(HashMap<String, Double> NGramsProbability, String sentence){
        sentence = LanguageModelMaker.START_DELIMITER + " " + cleanTitle(sentence) + " " + LanguageModelMaker.END_DELIMITER;
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

    }

}
