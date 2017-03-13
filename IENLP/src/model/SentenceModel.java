package model;



import java.util.*;

import static model.Util.cleanTitle;

/**
 * Created by Joep on 08-Mar-17.
 */
public class SentenceModel {

    private String sentence;
    private HashMap<String, Double> NGramsProbability;
    private HashMap<Integer, HashMap<String, Double>> segments;
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
        this.sentence = modelString(sentence);
        this.NGramsProbability = NGramsProbability;
    }

    public void createSentenceSegments() {
        this.segments = new HashMap<>();

        for(int i = 1; i <= this.sentence.length(); i++) {
            this.segments.put(i, createSentenceSegments(this.sentence, i));
        }
    }

    public HashMap<String, Double> createSentenceSegments(String sentence, Integer I){
        //Algorithm 1: Tweet Segmentation sigir12twiner
        HashMap<String, Double> segments = new HashMap<>();
        String[] sentenceWords = sentence.split("\\s");

        for (int i = I; i < sentenceWords.length - 1; i++){
            String[] tempSegment = Arrays.copyOfRange(sentenceWords,0,i + 1);

            if(i <= u) { //do not split
                double stickyness = segmentStickyness(tempSegment);
                segments.put(String.join(" ", tempSegment), stickyness);
            }

            for (int j = 1; j < i ;j++){
                // try different possible ways to segment
                if(i - j <= u){
                    //form two shorter segments

                    String[] splitTempSegment1 = Arrays.copyOfRange(tempSegment,0,j);
                    String[] splitTempSegment2 = Arrays.copyOfRange(tempSegment,j + 1,tempSegment.length - 1);

                    String splitTempSegment1String = String.join(" ",splitTempSegment1);
                    String splitTempSegment2String = String.join(" ",splitTempSegment2);
                    double sticknessSplitTempSegment2 = segmentStickyness(splitTempSegment2String);

                    HashMap<String, Double> segmentsIterator = this.segments.get(j);

                    for (Map.Entry<String, Double> entry: segmentsIterator.entrySet() ) {

                        String newSegmentation = entry.getKey() +  " " + splitTempSegment2String;

                        double stickyness = sticknessSplitTempSegment2 + segmentStickyness(entry.getKey());
                        segments.put(newSegmentation, stickyness);
                    }

                }

                //sort
                Map<String, Double> sorted = Util.sortByValue(segments);
                int index = e;
                for (Map.Entry<String,Double> entry: sorted.entrySet()) {
                    if(index-- < 0) {
                        break;
                    }
                    segments.put(entry.getKey(),entry.getValue());
                }

            }
        }
        return segments;
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
