package model;


import java.util.*;

import static jdk.nashorn.internal.objects.NativeString.trim;


/**
 * Created by peterwessels on 01/03/2017.
 */
public class NGram {
    private List<String> ngrams = new ArrayList<>();
    private HashMap<String, Double> ngramsP = new HashMap<>();

    public NGram() {

    }

    /**
     *
     * @param ngram
     * @param total_text
     * @return
     */
    public double p(String ngram, String total_text, int count) {
        int n = ngram.split("\\s").length;

        int current = Util.countSubstring(this.get(n), total_text);
        int previous = Util.countSubstring(this.get(n-1), total_text);

        double result = laPlaceSmoothing(current, previous, count);

        return result;
    }

    public double laPlaceSmoothing(Integer current, Integer previous, int V) {
        return ((double) current + 1d) / ((double) previous + (double) V);
    }

    /**
     *
     * @param total_text
     * @return
     */
    public HashMap<String, Double> getAllP(String total_text, int[] counts) {
        for(String ngram: ngrams) {
            int length = ngram.split("\\s").length;
            if(length > 1) {
                int count = counts[length - 1];
                this.ngramsP.put(ngram, this.p(ngram, total_text, count));
            }
        }

        return ngramsP;
    }

    /**
     *
     * @param gram
     */
    public void add(String gram) {
        String[] words = gram.split("\\s");
        if(words.length > 0) {
            this.ngrams.add(words.length - 1, trim(gram));
        }
    }

    /**
     *
     * @param n
     * @return
     */
    public String get(Integer n) {
        return ngrams.get(n - 1);
    }

    /**
     *
     * @return
     */
    public String toString() {
        String result = "N-Gram (" + this.ngrams.size() + ") \n";

        for(int i = 1; i < this.ngrams.size(); i++) {
            result += "P(" + this.ngrams.get(i) + "|" + this.ngrams.get(i-1) + ")= " + this.ngramsP.get(this.ngrams.get(i)) + "\n";
        }

        return result;
    }

    public int getLength() {
        return this.ngrams.size();
    }
}
