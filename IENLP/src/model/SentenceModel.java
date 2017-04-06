package model;



import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.UnmodifiableIterator;

import java.util.*;

import static model.Util.cleanTitle;

/**
 * Created by Joep on 08-Mar-17.
 */
public class SentenceModel {

    private String sentence;
    private HashMap<String, Double> NGramsProbability;
    private HashMap<Integer, HashMap<String, Double>> segments;
    private HashMap<Integer, Set> Si;
    /*
    3.1 sigir12twiner
    u = 5 is a proper bound as the maximum
    length of a segment, which largely reduces the number of possible
    segmentations.
    **/
    private static int u = 5;
    /*
    3.1 sigir12twiner
    We also set e = 5 so that the segmentation only
    focuses on top-quality segments and are not stuck by trivial ones
    **/
    private static int e = 5;

    SentenceModel(HashMap<String, Double> NGramsProbability, String sentence){
        this.sentence = modelString(sentence);
        this.NGramsProbability = NGramsProbability;
    }

    public void createSentenceSegments() {
        HashMap<Integer, Set> Si = new HashMap<>();

        for(int i = 1; i <= this.sentence.length(); i++) {
            Si.put(i, createSentenceSegments(this.sentence, i));
        }
    }

    public Set createSentenceSegments(String sentence, Integer i){
        // Algorithm 1: Tweet Segmentation sigir12twiner
        // Initialize a set i = {} to store possible segmentation of segment
        Comparator<String> cmp = (o1, o2) -> {
            String[] parts1 = o1.split("|");
            double part1 = 0;
            for(String p : parts1) {
                part1 += segmentStickyness(p);
            }

            String[] parts2 = o1.split("|");
            double part2 = 0;
            for(String p : parts2) {
                part2 += segmentStickyness(p);
            }

            double result = part1 - part2;
            if(result == 0){
                return 0;
            } else if (result < 0) {
                return -1;
            } else {
                return 1;
            }
        };

        String[] sentenceWords = sentence.split("\\s");
        Set<String> segments = new TreeSet<>(cmp);

        String[] si = Arrays.copyOfRange(sentenceWords,0,i + 1);

        if(i <= u) {
            // do not split si
            segments.add(String.join(" ", si));

            return segments;
        }

        for (int j = i; j < i - 1; j++){
            if(i - j <= u){
                //form two shorter segments

                String[] splitTempSegment1 = Arrays.copyOfRange(si,1,j);
                String[] splitTempSegment2 = Arrays.copyOfRange(si,j + 1,i);

                String splitTempSegment1String = String.join(" ",splitTempSegment1);
                String splitTempSegment2String = String.join(" ",splitTempSegment2);

                Set<String> segmentsJ = Si.get(j);

                for (String entry : segmentsJ) {
                    String newSegmentation = entry +  "| " + splitTempSegment2String;
                    segments.add(newSegmentation);
                }

            }

            return ImmutableSet.copyOf(Iterables.limit(segments, e));
        }

        return null;
    }

    public double normalizatedStickyness(){//TODO formula (12) of sigir12twiner
        return 1d;
    }

    /**
     *
     * @param segment
     * @return
     */
    public double segmentStickyness(String segment){
        double scp = symmetricalConditionalProbability(segment);
        double stickyness = 2d/(1d + Math.pow(Math.E,-scp));//formula (9) sigir12twiner
        return stickyness;
    }

    /**
     *
     * @param segment
     * @return
     */
    public double segmentStickyness(String[] segment){
        return this.segmentStickyness(String.join(" ", segment));
    }

    /**
     *
     * @param segment
     * @return
     */
    public double symmetricalConditionalProbability(String segment){
        if (!this.NGramsProbability.containsKey(segment)){
            System.out.println("segment string does not excist in prob {" + segment + "}");
            return 0d;
        }

        double totalProbability = this.NGramsProbability.get(segment);
        String[]  segmentArray = segment.split("\\s");

        double prob = 1d;
        for (int i = 0; i < segmentArray.length - 1;i++){
            String s1 = String.join(" ",Arrays.copyOfRange(segmentArray,0,i));
            String s2 = String.join(" ",Arrays.copyOfRange(segmentArray,i + 1 ,segmentArray.length - 1));
            if(this.NGramsProbability.containsKey(s1)){
                prob *= this.NGramsProbability.get(s1);
            }//TODO what if not excist
            if(this.NGramsProbability.containsKey(s2)){
                prob *= this.NGramsProbability.get(s2);
            }

        }
        double divisor = (1d / (((double) segmentArray.length) - 1d)) * prob;
        double scg = Math.pow(totalProbability,2) / divisor; //formula (7) sigir12twiner; TODO not complete
        return scg;
    }

    public static String modelString(String sentence){
        return LanguageModelMaker.START_DELIMITER + " " + cleanTitle(sentence) + " " + LanguageModelMaker.END_DELIMITER;
    }


}
